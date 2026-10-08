document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "../index.html";
        return;
    }

    const petsGrid = document.getElementById("petsGrid");
    const emptyPetsState = document.getElementById("emptyPetsState");
    const addPetFooter = document.getElementById("addPetFooter");

    try {
        const response = await fetch("http://localhost:8080/api/mascotas", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (response.status === 401 || response.status === 403) {
            localStorage.removeItem("token");
            window.location.href = "../index.html";
            return;
        }

        const mascotas = await response.json();

        if (mascotas && mascotas.length > 0) {
            emptyPetsState.style.display = "none";
            petsGrid.style.display = "flex";
            addPetFooter.style.display = "block";

            petsGrid.innerHTML = mascotas.map(mascota => `
        <article class="pet-item-card">
          <div class="pet-item-header">
            <div class="pet-avatar-placeholder">
              <i class="fa-solid ${mascota.tipo === 'GATO' ? 'fa-cat' : 'fa-dog'}"></i>
            </div>
            <div class="pet-item-title">
              <h2>${mascota.nombre}</h2>
              <span>${mascota.raza || "Mestizo"} • ${mascota.tipo || "Mascota"}</span>
            </div>
          </div>

          <div class="pet-tags">
            <span class="badge-tag"><i class="fa-solid fa-cake-candles"></i> ${mascota.edad ? mascota.edad + ' años' : 'Edad sin definir'}</span>
            <span class="badge-tag"><i class="fa-solid fa-weight-scale"></i> ${mascota.tamano || 'Tamaño sin definir'}</span>
          </div>

          ${mascota.observacionesMedicas ? `
            <div class="pet-observations-preview">
              <strong>Salud:</strong> ${mascota.observacionesMedicas}
            </div>
          ` : ''}
        </article>
      `).join("");

        } else {
            petsGrid.style.display = "none";
            addPetFooter.style.display = "none";
            emptyPetsState.style.display = "block";
        }

    } catch (error) {
        console.error("Error al cargar las mascotas:", error);
    }
});