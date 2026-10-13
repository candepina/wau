document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");
    const userSession = JSON.parse(localStorage.getItem("userSession") || "{}");

    if (!token || userSession.rol !== "ROLE_GUARDERIA") {
        localStorage.clear();
        window.location.href = "../index.html";
        return;
    }

    const estadiaId = new URLSearchParams(window.location.search).get("id");
    if (!estadiaId) {
        window.location.href = "home.html";
        return;
    }

    const API = `http://localhost:8080/api/estadias/${estadiaId}/actividades`;

    const form = document.getElementById("nuevaActividadForm");
    const feedback = document.getElementById("formFeedback");
    const btnSubmit = document.getElementById("btnSubmit");
    const tipoSelect = document.getElementById("tipo");
    const validationInfo = document.getElementById("validationInfo");
    const validationText = document.getElementById("validationText");
    const btnVolver = document.getElementById("btnVolver");

    // Configurar href del botón volver
    btnVolver.href = `timeline.html?id=${estadiaId}`;

    // Configuración de validaciones por tipo
    const tipoValidaciones = {
        CHECK_IN: {
            mensaje: "Solo se permite un check-in por estadía. Si ya existe, no podrás registrar otro."
        },
        CHECK_OUT: {
            mensaje: "Solo se permite un check-out por estadía. Requiere un check-in previo."
        },
        PASEO: { mensaje: "" },
        COMIDA: { mensaje: "" },
        DESCANSO: { mensaje: "" },
        MEDICINA: { mensaje: "" }
    };

    function setLoading(loading) {
        btnSubmit.disabled = loading;
        btnSubmit.querySelector('.btn-spinner').style.display = loading ? 'inline' : 'none';
        btnSubmit.querySelector('.btn-text').style.display = loading ? 'none' : 'inline';
    }

    function showFeedback(msg, type) {
        feedback.textContent = msg;
        feedback.className = `feedback-msg ${type}`;
        feedback.style.display = 'block';
    }

    function authHeaders() {
        return { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' };
    }

    function logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('userSession');
        window.location.href = '../index.html';
    }

    // Mostrar info de validación al cambiar tipo
    tipoSelect.addEventListener('change', () => {
        const tipo = tipoSelect.value;
        const config = tipoValidaciones[tipo];
        if (config && config.mensaje) {
            validationText.textContent = config.mensaje;
            validationInfo.style.display = 'flex';
        } else {
            validationInfo.style.display = 'none';
        }
        feedback.style.display = 'none';
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        feedback.style.display = 'none';

        const tipo = tipoSelect.value;
        if (!tipo) {
            showFeedback('Seleccioná un tipo de actividad', 'error');
            return;
        }

        setLoading(true);
        try {
            const response = await fetch(`http://localhost:8080/api/estadias/${estadiaId}/actividades`, {
                method: 'POST',
                headers: authHeaders(),
                body: JSON.stringify({ tipo })
            });

            if (response.status === 401 || response.status === 403) {
                logout();
                return;
            }

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                const errorMsg = errorData.error || errorData.message || 'Error al registrar la actividad';
                showFeedback(errorMsg, 'error');
                return;
            }

            showFeedback('Actividad registrada correctamente', 'success');
            setTimeout(() => {
                window.location.href = `timeline.html?id=${estadiaId}`;
            }, 800);

        } catch (error) {
            console.error("Error:", error);
            showFeedback('Error de conexión', 'error');
        } finally {
            setLoading(false);
        }
    });
});