document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");

    // Validación de sesión activa
    if (!token) {
        window.location.href = "login.html";
        return;
    }

    const form = document.getElementById("registroMascotaForm");
    const feedback = document.getElementById("formFeedback");
    const btnSubmit = document.getElementById("btnSubmit");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        feedback.style.display = "none";

        // 1. Extraer datos del formulario
        const nombre = document.getElementById("nombre").value.trim();
        const tipo = document.getElementById("tipo").value;
        const edadVal = document.getElementById("edad").value;
        const edad = edadVal ? parseInt(edadVal, 10) : null;
        const raza = document.getElementById("raza").value.trim();
        const tamano = document.getElementById("tamano").value;

        const alimentacion = document.getElementById("alimentacion").value.trim();
        const alergias = document.getElementById("alergias").value.trim();
        const medicacion = document.getElementById("medicacion").value.trim();
        const conductuales = document.getElementById("conductuales").value.trim();

        // 2. Mapear observaciones semánticas a las columnas del backend
        const observacionesMedicas = [
            alergias ? `Alergias: ${alergias}` : null,
            medicacion ? `Medicación: ${medicacion}` : null
        ].filter(Boolean).join(" | ");

        const payload = {
            nombre,
            tipo,
            raza: raza || "Mestizo",
            edad,
            tamano,
            observacionesMedicas: observacionesMedicas || null,
            observacionesConductuales: conductuales || null,
            observacionesGenerales: alimentacion || null
        };

        // 3. Estado de carga en botón
        btnSubmit.disabled = true;
        btnSubmit.textContent = "Registrando...";

        try {
            const response = await fetch("http://localhost:8080/api/mascotas", {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || "No se pudo registrar la mascota.");
            }

            feedback.textContent = "¡Mascota registrada correctamente!";
            feedback.className = "feedback-msg success";
            feedback.style.display = "block";

            // Redirección a la lista de mascotas
            setTimeout(() => {
                window.location.href = "mis-mascotas.html";
            }, 900);

        } catch (error) {
            feedback.textContent = error.message;
            feedback.className = "feedback-msg error";
            feedback.style.display = "block";
            btnSubmit.disabled = false;
            btnSubmit.textContent = "Registrar mascota";
        }
    });
});