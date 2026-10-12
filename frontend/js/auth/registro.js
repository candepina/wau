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
        clearFieldErrors();

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
                const contentType = res.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    const errorData = await res.json();
                    if (errorData.errors) {
                        // Errores de validación por campo
                        Object.entries(errorData.errors).forEach(([field, message]) => {
                            showFieldError(field, message);
                        });
                        showFeedback('Por favor corregí los errores marcados.', 'error');
                    } else if (errorData.error) {
                        // Error global (ej: email duplicado)
                        showFeedback(errorData.error, 'error');
                    } else {
                        showFeedback('Error en el registro', 'error');
                    }
                } else {
                    const errText = await res.text();
                    showFeedback(errText || 'Error en el registro', 'error');
                }
                setLoading(false);
                return;
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

    function showFieldError(fieldName, message) {
        const input = document.getElementById(fieldName);
        if (input) {
            input.classList.add('input-error');
            // Remover error previo si existe
            const existingError = input.parentNode.querySelector('.field-error');
            if (existingError) existingError.remove();

            const errorSpan = document.createElement('span');
            errorSpan.className = 'field-error';
            errorSpan.textContent = message;
            input.parentNode.appendChild(errorSpan);
        }
    }

    function clearFieldErrors() {
        document.querySelectorAll('.input-error').forEach(input => {
            input.classList.remove('input-error');
        });
        document.querySelectorAll('.field-error').forEach(span => span.remove());
    }

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