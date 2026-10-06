document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    const petCard = document.getElementById("petCard");
    const emptyPetCard = document.getElementById("emptyPetCard");
    const petActivitySection = document.getElementById("petActivitySection");

    const petNameEl = document.getElementById("petName");
    const petStatusEl = document.getElementById("petStatusText");

    // Elementos de Actualizaciones
    const timelineList = document.getElementById("timelineList");
    const emptyUpdates = document.getElementById("emptyUpdates");
    const linkVerActualizaciones = document.getElementById("linkVerActualizaciones");

    // Elementos de Galería
    const galleryList = document.getElementById("galleryList");
    const emptyGallery = document.getElementById("emptyGallery");
    const linkVerGaleria = document.getElementById("linkVerGaleria");

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
            window.location.href = "login.html";
            return;
        }

        const mascotas = await response.json();

        if (mascotas && mascotas.length > 0) {
            // 1. Mostrar tarjeta de la mascota
            const primeraMascota = mascotas[0];
            petNameEl.textContent = primeraMascota.nombre;
            petStatusEl.textContent = `Raza: ${primeraMascota.raza || "Mestizo"}`;

            petCard.style.display = "flex";
            petActivitySection.style.display = "block";
            emptyPetCard.style.display = "none";

            // 2. Evaluar Actualizaciones (por ahora simulamos vacío hasta crear la entidad en el backend)
            const actualizaciones = []; // Vendrá de la API más adelante
            renderActualizaciones(actualizaciones);

            // 3. Evaluar Galería de Fotos (simulamos vacío)
            const fotos = []; // Vendrá de la API más adelante
            renderGaleria(fotos);

        } else {
            // Usuario sin mascota
            petCard.style.display = "none";
            petActivitySection.style.display = "none";
            emptyPetCard.style.display = "block";
        }

    } catch (error) {
        console.error("Error al conectar con la API de mascotas:", error);
    }

    function renderActualizaciones(items) {
        if (items && items.length > 0) {
            emptyUpdates.style.display = "none";
            linkVerActualizaciones.style.display = "inline";
            timelineList.style.display = "flex";
            timelineList.innerHTML = items.map(item => `
        <div class="timeline-item">
          <div class="timeline-icon ${item.tipo === 'PASEO' ? 'icon-walk' : 'icon-food'}">
            <i class="fa-solid ${item.tipo === 'PASEO' ? 'fa-person-walking' : 'fa-utensils'}"></i>
          </div>
          <div class="timeline-text">
            <strong>${item.titulo}</strong>
            <small>${item.hora}</small>
          </div>
        </div>
      `).join("");
        } else {
            emptyUpdates.style.display = "flex";
            linkVerActualizaciones.style.display = "none";
            timelineList.style.display = "none";
        }
    }

    function renderGaleria(fotos) {
        if (fotos && fotos.length > 0) {
            emptyGallery.style.display = "none";
            linkVerGaleria.style.display = "inline";
            galleryList.style.display = "flex";
            galleryList.innerHTML = fotos.map(src => `
        <img src="${src}" alt="Foto estadía" />
      `).join("");
        } else {
            emptyGallery.style.display = "flex";
            linkVerGaleria.style.display = "none";
            galleryList.style.display = "none";
        }
    }
});