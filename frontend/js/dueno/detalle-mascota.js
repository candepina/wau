const API = 'http://localhost:8080/api/mascotas';
const token = localStorage.getItem('token');
if (!token) location.href = '../index.html';

const form = document.getElementById('detalleMascotaForm');
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

const mascotaId = new URLSearchParams(window.location.search).get('id');
if (!mascotaId) location.href = 'mis-mascotas.html';

let datosOriginales = null;

async function cargarMascota() {
    const res = await fetch(`${API}/${mascotaId}`, { headers: authHeaders() });
    if (res.status === 401 || res.status === 403) { logout(); return; }
    if (!res.ok) { location.href = 'mis-mascotas.html'; return; }
    const m = await res.json();
    poblarForm(m);
    datosOriginales = { ...m };
    setModo('vista');
}

function poblarForm(m) {
    document.getElementById('nombre').value = m.nombre ?? '';
    document.getElementById('raza').value = m.raza ?? '';
    document.getElementById('edad').value = m.edad ?? '';
    document.getElementById('tamano').value = m.tamano ?? '';
    const [alergias = '', medicacion = ''] = (m.observacionesMedicas ?? '').split(' | ');
    document.getElementById('alergias').value = alergias.replace('Alergias: ', '');
    document.getElementById('medicacion').value = medicacion.replace('Medicación: ', '');
    document.getElementById('conductuales').value = m.observacionesConductuales ?? '';
    document.getElementById('alimentacion').value = m.observacionesGenerales ?? '';
}

function setModo(modo) {
    const disabled = modo === 'vista';
    form.querySelectorAll('input, select, textarea').forEach(el => el.disabled = disabled);
    actionsView.style.display = modo === 'vista' ? 'flex' : 'none';
    actionsEdit.style.display = modo === 'edicion' ? 'flex' : 'none';
    pageTitle.textContent = modo === 'vista' ? 'DETALLE MASCOTA' : 'EDITAR MASCOTA';
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
    const alergias = document.getElementById('alergias').value.trim();
    const medicacion = document.getElementById('medicacion').value.trim();
    const obsMedicas = [alergias ? `Alergias: ${alergias}` : null, medicacion ? `Medicación: ${medicacion}` : null].filter(Boolean).join(' | ');
    return {
        nombre: document.getElementById('nombre').value.trim(),
        raza: document.getElementById('raza').value.trim() || 'Mestizo',
        edad: document.getElementById('edad').value ? parseInt(document.getElementById('edad').value) : null,
        tamano: document.getElementById('tamano').value,
        observacionesMedicas: obsMedicas || null,
        observacionesConductuales: document.getElementById('conductuales').value.trim() || null,
        observacionesGenerales: document.getElementById('alimentacion').value.trim() || null
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
        const sheet = overlay.querySelector('.modal-bottom-sheet');
        
        const cleanup = () => {
            overlay.style.display = 'none';
            confirmBtn.removeEventListener('click', onConfirm);
            cancelBtn.removeEventListener('click', onCancel);
            overlay.removeEventListener('click', onOverlayClick);
        };
        
        const onConfirm = () => { cleanup(); resolve(true); };
        const onCancel = () => { cleanup(); resolve(false); };
        const onOverlayClick = (e) => {
            // Solo cerrar si se clickea el overlay (fondo), no el sheet
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
    if (!document.getElementById('nombre').value.trim()) {
        showFeedback('El nombre es obligatorio', 'error');
        return;
    }
    setLoading(btnSave, true);
    try {
        const res = await fetch(`${API}/${mascotaId}`, {
            method: 'PUT', headers: authHeaders(), body: JSON.stringify(construirPayload())
        });
        if (res.status === 401 || res.status === 403) { logout(); return; }
        if (!res.ok) {
            const err = await res.json();
            showFeedback(err.error || err.errors?.[0] || 'Error al actualizar', 'error');
            return;
        }
        const updated = await res.json();
        showToast('Mascota actualizada correctamente', 'success');
        datosOriginales = { ...updated };
        setModo('vista');
        poblarForm(updated);
    } catch (err) {
        showFeedback('Error de conexión', 'error');
    } finally {
        setLoading(btnSave, false);
    }
});

async function eliminarMascota() {
    const confirmed = await showConfirmModal(
        'Eliminar mascota',
        '¿Eliminar a esta mascota? No se puede deshacer.'
    );
    if (!confirmed) return;
    
    const btn = document.activeElement.id === 'btnDelete' ? btnDelete : btnDeleteEdit;
    setLoading(btn, true);
    try {
        const res = await fetch(`${API}/${mascotaId}`, { method: 'DELETE', headers: authHeaders() });
        if (res.status === 401 || res.status === 403) { logout(); return; }
        if (res.status === 409) {
            const err = await res.json();
            showFeedback(err.error || 'La mascota tiene una estadía activa y no puede eliminarse', 'error');
            return;
        }
        if (!res.ok) { showFeedback('Error al eliminar', 'error'); return; }
        showToast('Mascota eliminada correctamente', 'success');
        setTimeout(() => location.href = 'mis-mascotas.html', 800);
    } catch (err) {
        showFeedback('Error de conexión', 'error');
    } finally {
        setLoading(btn, false);
    }
}

btnDelete.addEventListener('click', eliminarMascota);
btnDeleteEdit.addEventListener('click', eliminarMascota);

cargarMascota();