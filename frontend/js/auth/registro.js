const API_URL = 'http://localhost:8080/api/auth/registro';

let rolSeleccionado = 'ROLE_DUENO';

const btnRoleDueno = document.getElementById('btnRoleDueno');
const btnRoleGuarderia = document.getElementById('btnRoleGuarderia');
const camposDueno = document.getElementById('camposDueno');
const camposGuarderia = document.getElementById('camposGuarderia');
const form = document.getElementById('registroForm');
const feedback = document.getElementById('feedbackMessage');
const submitBtn = document.getElementById('submitBtn');

// Alternar entre Dueño y Guardería
btnRoleDueno.addEventListener('click', () => {
    rolSeleccionado = 'ROLE_DUENO';
    btnRoleDueno.classList.add('active');
    btnRoleGuarderia.classList.remove('active');
    camposDueno.classList.remove('hidden');
    camposGuarderia.classList.add('hidden');
});

btnRoleGuarderia.addEventListener('click', () => {
    rolSeleccionado = 'ROLE_GUARDERIA';
    btnRoleGuarderia.classList.add('active');
    btnRoleDueno.classList.remove('active');
    camposGuarderia.classList.remove('hidden');
    camposDueno.classList.add('hidden');
});

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearFeedback();

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const telefono = document.getElementById('telefono').value.trim();

    if (!email || !password) {
        showFeedback('Completá al menos el correo y la contraseña.', 'error');
        return;
    }

    // Armar el payload dinámicamente según el rol
    const payload = {
        email,
        password,
        telefono,
        rol: rolSeleccionado
    };

    if (rolSeleccionado === 'ROLE_DUENO') {
        payload.nombre = document.getElementById('nombre').value.trim();
        payload.apellido = document.getElementById('apellido').value.trim();
        payload.dni = document.getElementById('dni').value.trim();
        payload.direccionDomicilio = document.getElementById('direccionDomicilio').value.trim();
    } else {
        payload.nombreEstablecimiento = document.getElementById('nombreEstablecimiento').value.trim();
        payload.nombreResponsable = document.getElementById('nombreResponsable').value.trim();
        payload.direccionPredio = document.getElementById('direccionPredio').value.trim();
    }

    setLoading(true);

    try {
        const res = await fetch(API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const errText = await res.text();
            throw new Error(errText || 'Error en el registro');
        }

        const data = await res.json();
        showFeedback('¡Cuenta creada con éxito! Redirigiendo al login...', 'success');

        setTimeout(() => {
            window.location.href = 'index.html';
        }, 1200);

    } catch (error) {
        showFeedback(error.message, 'error');
    } finally {
        setLoading(false);
    }
});

function showFeedback(msg, type) {
    feedback.textContent = msg;
    feedback.className = `feedback ${type}`;
}

function clearFeedback() {
    feedback.textContent = '';
    feedback.className = 'feedback';
}

function setLoading(isLoading) {
    submitBtn.disabled = isLoading;
    submitBtn.classList.toggle('loading', isLoading);
}