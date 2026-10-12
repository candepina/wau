const API = 'http://localhost:8080/api/perfil';
const token = localStorage.getItem('token');
const userSession = JSON.parse(localStorage.getItem('userSession') || '{}');

if (!token || userSession.rol !== 'ROLE_DUENO') {
    location.href = '../index.html';
}

const form = document.getElementById('perfilForm');
const feedback = document.getElementById('formFeedback');
const actionsView = document.getElementById('actionsView');
const actionsEdit = document.getElementById('actionsEdit');
const btnEdit = document.getElementById('btnEdit');
const btnEditMain = document.getElementById('btnEditMain');
const btnCancel = document.getElementById('btnCancel');
const btnSave = document.getElementById('btnSave');
const btnDelete = document.getElementById('btnDelete');
const btnDeleteEdit = document.getElementById('btnDeleteEdit');
const pageTitle = document.getElementById('pageTitle');

let datosOriginales = null;

async function cargarPerfil() {
    const res = await fetch(API, { headers: authHeaders() });
    if (res.status === 401 || res.status === 403) { logout(); return; }
    if (!res.ok) { location.href = 'home.html'; return; }
    const data = await res.json();
    poblarForm(data);
    datosOriginales = { ...data };
    setModo('vista');
}

function poblarForm(data) {
    document.getElementById('email').value = data.email ?? '';
    document.getElementById('rol').value = data.rol === 'ROLE_DUENO' ? 'Dueño' : 'Guardería';
    document.getElementById('fechaAlta').value = data.fechaAlta ? new Date(data.fechaAlta).toLocaleDateString('es-AR') : '';
    document.getElementById('nombre').value = data.nombre ?? '';
    document.getElementById('apellido').value = data.apellido ?? '';
    document.getElementById('dni').value = data.dni ?? '';
    document.getElementById('direccionDomicilio').value = data.direccionDomicilio ?? '';
    document.getElementById('telefono').value = data.telefono ?? '';
    // Password fields vacíos
    document.getElementById('passwordActual').value = '';
    document.getElementById('passwordNuevo').value = '';
    document.getElementById('passwordNuevoConfirmacion').value = '';
}

function setModo(modo) {
    const disabled = modo === 'vista';
    const inputs = form.querySelectorAll('input:not(#email):not(#rol):not(#fechaAlta)');
    inputs.forEach(el => el.disabled = disabled);
    actionsView.style.display = modo === 'vista' ? 'flex' : 'none';
    actionsEdit.style.display = modo === 'edicion' ? 'flex' : 'none';
    pageTitle.textContent = modo === 'vista' ? 'MI PERFIL' : 'EDITAR PERFIL';
    feedback.style.display = 'none';
}

function setLoading(btn, loading) {
    btn.disabled = loading;
    const spinner = btn.querySelector('.btn-spinner');
    const text = btn.querySelector('.btn-text');
    if (spinner) spinner.style.display = loading ? 'inline' : 'none';
    if (text) text.style.display = loading ? 'none' : 'inline';
}

function authHeaders() {
    return { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' };
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userSession');
    location.href = '../index.html';
}

function construirPayload() {
    return {
        telefono: document.getElementById('telefono').value.trim(),
        nombre: document.getElementById('nombre').value.trim(),
        apellido: document.getElementById('apellido').value.trim(),
        dni: document.getElementById('dni').value.trim(),
        direccionDomicilio: document.getElementById('direccionDomicilio').value.trim(),
        passwordActual: document.getElementById('passwordActual').value || null,
        passwordNuevo: document.getElementById('passwordNuevo').value || null,
        passwordNuevoConfirmacion: document.getElementById('passwordNuevoConfirmacion').value || null
    };
}

function showFeedback(msg, type) {
    feedback.textContent = msg;
    feedback.className = `feedback-msg ${type}`;
    feedback.style.display = 'block';
}

// Toast notification
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    const icons = { success: 'fa-circle-check', error: 'fa-circle-xmark', info: 'fa-circle-info' };
    toast.innerHTML = `
        <i class="fa-solid toast-icon ${icons[type] || icons.success}"></i>
        <span class="toast-message">${message}</span>
        <button class="toast-close" aria-label="Cerrar"><i class="fa-solid fa-xmark"></i></button>
    `;
    container.appendChild(toast);

    toast.querySelector('.toast-close').addEventListener('click', () => toast.remove());

    setTimeout(() => {
        toast.style.animation = 'toastIn 0.3s ease reverse';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Modal de confirmación (Bottom Sheet)
function showConfirmModal(title, message) {
    return new Promise((resolve) => {
        document.getElementById('modalTitle').textContent = title;
        document.getElementById('modalMessage').textContent = message;
        const overlay = document.getElementById('modalOverlay');
        overlay.style.display = 'flex';

        const confirmBtn = document.getElementById('modalConfirm');
        const cancelBtn = document.getElementById('modalCancel');

        const cleanup = () => {
            overlay.style.display = 'none';
            confirmBtn.removeEventListener('click', onConfirm);
            cancelBtn.removeEventListener('click', onCancel);
            overlay.removeEventListener('click', onOverlayClick);
        };

        const onConfirm = () => { cleanup(); resolve(true); };
        const onCancel = () => { cleanup(); resolve(false); };
        const onOverlayClick = (e) => {
            if (e.target === overlay) { cleanup(); resolve(false); }
        };

        confirmBtn.addEventListener('click', onConfirm);
        cancelBtn.addEventListener('click', onCancel);
        overlay.addEventListener('click', onOverlayClick);
    });
}

btnEdit.addEventListener('click', () => setModo('edicion'));
btnEditMain.addEventListener('click', () => setModo('edicion'));

btnCancel.addEventListener('click', () => {
    poblarForm(datosOriginales);
    setModo('vista');
});

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    // Validaciones básicas
    if (!document.getElementById('nombre').value.trim()) {
        showFeedback('El nombre es obligatorio', 'error');
        return;
    }
    if (!document.getElementById('apellido').value.trim()) {
        showFeedback('El apellido es obligatorio', 'error');
        return;
    }
    if (!document.getElementById('dni').value.trim()) {
        showFeedback('El DNI es obligatorio', 'error');
        return;
    }
    if (!document.getElementById('direccionDomicilio').value.trim()) {
        showFeedback('La dirección es obligatoria', 'error');
        return;
    }
    if (!document.getElementById('telefono').value.trim()) {
        showFeedback('El teléfono es obligatorio', 'error');
        return;
    }

    // Validar password si se quiere cambiar
    const passwordNuevo = document.getElementById('passwordNuevo').value;
    const passwordActual = document.getElementById('passwordActual').value;
    const passwordConfirm = document.getElementById('passwordNuevoConfirmacion').value;

    if (passwordNuevo && !passwordActual) {
        showFeedback('Debes ingresar tu contraseña actual para cambiarla', 'error');
        return;
    }
    if (passwordNuevo && passwordNuevo !== passwordConfirm) {
        showFeedback('Las nuevas contraseñas no coinciden', 'error');
        return;
    }

    setLoading(btnSave, true);
    try {
        const res = await fetch(API, {
            method: 'PUT', headers: authHeaders(), body: JSON.stringify(construirPayload())
        });
        if (res.status === 401 || res.status === 403) { logout(); return; }
        if (!res.ok) {
            const err = await res.json();
            showFeedback(err.error || err.errors?.[0] || 'Error al actualizar', 'error');
            return;
        }
        const updated = await res.json();
        showToast('Perfil actualizado correctamente', 'success');
        datosOriginales = { ...updated };
        setModo('vista');
        poblarForm(updated);
    } catch (err) {
        showFeedback('Error de conexión', 'error');
    } finally {
        setLoading(btnSave, false);
    }
});

async function eliminarCuenta() {
    const confirmed = await showConfirmModal(
        'Eliminar cuenta',
        '¿Eliminar tu cuenta? Se borrarán todos tus datos permanentemente, incluyendo tus mascotas. No se puede deshacer.'
    );
    if (!confirmed) return;

    const btn = document.activeElement.id === 'btnDelete' ? btnDelete : btnDeleteEdit;
    setLoading(btn, true);
    try {
        const res = await fetch(API, { method: 'DELETE', headers: authHeaders() });
        if (res.status === 401 || res.status === 403) { logout(); return; }
        if (res.status === 409) {
            const err = await res.json();
            showFeedback(err.error || 'No se puede eliminar la cuenta porque tienes mascotas con estadías activas', 'error');
            return;
        }
        if (!res.ok) { showFeedback('Error al eliminar la cuenta', 'error'); return; }
        showToast('Cuenta eliminada correctamente', 'success');
        setTimeout(() => logout(), 800);
    } catch (err) {
        showFeedback('Error de conexión', 'error');
    } finally {
        setLoading(btn, false);
    }
}

btnDelete.addEventListener('click', eliminarCuenta);
btnDeleteEdit.addEventListener('click', eliminarCuenta);

cargarPerfil();