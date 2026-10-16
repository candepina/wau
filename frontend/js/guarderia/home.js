document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("token");
    const userSession = JSON.parse(localStorage.getItem("userSession") || "{}");

    if (!token || userSession.rol !== "ROLE_GUARDERIA") {
        localStorage.clear();
        window.location.href = "../index.html";
        return;
    }

    // Logout
    const btnLogout = document.getElementById("btnLogout");
    if (btnLogout) {
        btnLogout.addEventListener("click", () => {
            localStorage.clear();
            window.location.href = "../index.html";
        });
    }

    const guestsContainer = document.getElementById("guestsContainer");
    const emptyGuestsState = document.getElementById("emptyGuestsState");
    const guestCounter = document.getElementById("guestCounter");

    // Función reutilizable para obtener huéspedes (activas + programadas)
    async function cargarEstadiasActivas() {
        try {
            const response = await fetch("http://localhost:8080/api/estadias", {
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            });

            if (response.status === 401 || response.status === 403) {
                localStorage.clear();
                window.location.href = "../index.html";
                return;
            }

            if (!response.ok) {
                throw new Error("Error al obtener los huéspedes del predio");
            }

            const estadias = await response.json();

            if (estadias && estadias.length > 0) {
                emptyGuestsState.style.display = "none";
                guestsContainer.style.display = "flex";
                const activas = estadias.filter(e => e.estado === "ACTIVA").length;
                const programadas = estadias.filter(e => e.estado === "PROGRAMADA").length;
                guestCounter.textContent = `${activas} ${activas === 1 ? "perro hospedado" : "perros hospedados"}${programadas > 0 ? ` + ${programadas} programada${programadas === 1 ? "" : "s"}` : ""}`;

                guestsContainer.innerHTML = estadias.map(estadia => {
                const esProgramada = estadia.estado === "PROGRAMADA";
                return `
          <article class="guest-card" id="estadia-${estadia.id}">
            <div class="guest-card-header">
              <div class="guest-avatar">
                <i class="fa-solid fa-dog"></i>
              </div>
              <div class="guest-info">
                <h2>${estadia.nombreMascota}</h2>
                <span>${estadia.razaMascota || "Mestizo"} • Dueño: ${estadia.nombreDueno}</span>
              </div>
            </div>

            <div class="guest-badges">
              ${esProgramada ? `
              <span class="guest-badge badge-programada">
                <i class="fa-solid fa-clock"></i> Programada
              </span>` : `
              <span class="guest-badge badge-active">
                <i class="fa-solid fa-circle-dot"></i> Activo
              </span>`}
              <span class="guest-badge">
                <i class="fa-regular fa-calendar"></i> Ingreso: ${estadia.fechaInicio}
              </span>
            </div>

            ${estadia.notasIngreso ? `
              <div class="guest-notes">
                <strong>Indicaciones:</strong> ${estadia.notasIngreso}
              </div>
            ` : ""}

            <div class="guest-actions">
              <a href="timeline.html?id=${estadia.id}" class="btn-guest-action primary">
                <i class="fa-solid fa-clock-rotate-left"></i> Timeline
              </a>
              <button class="btn-guest-action danger" onclick="finalizarEstadia(${estadia.id}, '${estadia.nombreMascota}')">
                <i class="fa-solid fa-right-from-bracket"></i> ${esProgramada ? "Cancelar" : "Finalizar"}
              </button>
            </div>
          </article>
        `}).join("");

            } else {
                guestsContainer.style.display = "none";
                guestCounter.textContent = "0 huéspedes en el predio";
                emptyGuestsState.style.display = "block";
            }

        } catch (error) {
            console.error("Error al cargar estadías:", error);
            guestCounter.textContent = "Error al sincronizar datos";
        }
    }

    // Función global para dar de baja la estadía
    window.finalizarEstadia = async (id, nombreMascota) => {
        const confirmar = confirm(`¿Confirmás el egreso de ${nombreMascota}? La estadía quedará finalizada.`);
        if (!confirmar) return;

        try {
            const response = await fetch(`http://localhost:8080/api/estadias/${id}/finalizar`, {
                method: "PATCH",
                headers: {
                    "Authorization": `Bearer ${token}`
                }
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || "No se pudo finalizar la estadía.");
            }

            // Recargar la lista actualizada
            await cargarEstadiasActivas();

        } catch (error) {
            alert("Error: " + error.message);
        }
    };

    // Carga inicial
    cargarEstadiasActivas();
});