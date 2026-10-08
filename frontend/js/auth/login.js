const API_URL = 'http://localhost:8080/api/auth/login';

const loginForm = document.getElementById('loginForm');
const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const submitBtn = document.getElementById('submitBtn');
const feedback = document.getElementById('feedbackMessage');
const togglePasswordBtn = document.getElementById('togglePassword');

// Alternar visibilidad de contraseña
togglePasswordBtn.addEventListener('click', () => {
    const isPassword = passwordInput.getAttribute('type') === 'password';
    passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
    togglePasswordBtn.textContent = isPassword ? '🙈' : '👁';
});

// Manejo del formulario
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearFeedback();

    const email = emailInput.value.trim();
    const password = passwordInput.value;

    if (!email || !password) {
        showFeedback('Por favor completá todos los campos.', 'error');
        return;
    }

    setLoading(true);

    try {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || 'Error al iniciar sesión.');
        }

        const data = await response.json();

        // Guardar token y datos del usuario
        localStorage.setItem('token', data.token);
        localStorage.setItem('userSession', JSON.stringify({
            id: data.id,
            email: data.email,
            rol: data.rol
        }));

        showFeedback('Ingreso correcto. Redirigiendo...', 'success');

        // Redirección según rol
        setTimeout(() => {
            if (data.rol === 'ROLE_DUENO') {
                window.location.href = 'dueno/home.html';
            } else if (data.rol === 'ROLE_GUARDERIA') {
                alert(`¡Bienvenida guardería! (ID: ${data.id})`);
                // window.location.href = 'dashboard-guarderia.html';
            }
        }, 800);

    } catch (error) {
        showFeedback(error.message, 'error');
    } finally {
        setLoading(false);
    }
});

function showFeedback(message, type) {
    feedback.textContent = message;
    feedback.className = `feedback ${type}`;
}

function clearFeedback() {
    feedback.textContent = '';
    feedback.className = 'feedback';
}

function setLoading(isLoading) {
    if (isLoading) {
        submitBtn.classList.add('loading');
        submitBtn.disabled = true;
    } else {
        submitBtn.classList.remove('loading');
        submitBtn.disabled = false;
    }
}