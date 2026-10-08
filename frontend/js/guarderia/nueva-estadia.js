document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");
    const userSession = JSON.parse(localStorage.getItem("userSession") || "{}");

    // Validación de seguridad y rol
    if (!token || userSession.rol !== "ROLE_GUARDERIA") {
        localStorage.clear();
        window.location.href = "../index.html";
        return;
    }

    // Pre-cargar la fecha de hoy automáticamente en fechaInicio
    const inputFechaInicio = document.getElementById("fechaInicio");
    const inputFechaFin = document.getElementById("fechaFin");
    const inputCodigo = document.getElementById("codigoMascota");

    const todayStr = new Date().toISOString().split("T")[0];
    inputFechaInicio.value = todayStr;
    inputFechaFin.min = todayStr;

    // Ajustar fecha mínima de salida si cambia la de ingreso
    inputFechaInicio.addEventListener("change", () => {
        inputFechaFin.min = inputFechaInicio.value;
        if (inputFechaFin.value && inputFechaFin.value < inputFechaInicio.value) {
            inputFechaFin.value = inputFechaInicio.value;
        }
    });

    // Forzar mayúsculas en el código mientras escribe
    inputCodigo.addEventListener("input", (e) => {
        e.target.value = e.target.value.toUpperCase().replace(/\s+/g, '');
    });

    const form = document.getElementById("nuevaEstadiaForm");
    const feedback = document.getElementById("formFeedback");
    const btnSubmit = document.getElementById("btnSubmit");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        feedback.style.display = "none";

        const codigoMascota = inputCodigo.value.trim().toUpperCase();
        const fechaInicio = inputFechaInicio.value;
        const fechaFin = inputFechaFin.value || null;
        const notasIngreso = document.getElementById("notasIngreso").value.trim() || null;

        const payload = {
            codigoMascota,
            fechaInicio,
            fechaFin,
            notasIngreso
        };

        // Estado visual de carga
        btnSubmit.disabled = true;
        btnSubmit.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Procesando ingreso...`;

        try {
            const response = await fetch("http://localhost:8080/api/estadias", {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                let errorMsg = "No se pudo registrar la estadía.";
                try {
                    const errData = await response.json();
                    errorMsg = errData.message || errorMsg;
                } catch (_) {
                    const textErr = await response.text();
                    if (textErr) errorMsg = textErr;
                }
                throw new Error(errorMsg);
            }

            const estadiaCreada = await response.json();

            feedback.textContent = `¡Ingreso confirmado! ${estadiaCreada.nombreMascota} ya está en el predio.`;
            feedback.className = "feedback-msg success";
            feedback.style.display = "block";

            // Redirigir al Home de la guardería para ver la tarjeta creada
            setTimeout(() => {
                window.location.href = "home.html";
            }, 900);

        } catch (error) {
            feedback.textContent = error.message;
            feedback.className = "feedback-msg error";
            feedback.style.display = "block";
            btnSubmit.disabled = false;
            btnSubmit.innerHTML = `<i class="fa-solid fa-check"></i> Confirmar Check-in`;
        }
    });
});