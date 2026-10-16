document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "../index.html";
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

    const TITULOS_ACTIVIDAD = {
        CHECK_IN: "Check-in",
        PASEO: "Paseo",
        COMIDA: "Comida",
        DESCANSO: "Descanso",
        MEDICINA: "Medicina",
        CHECK_OUT: "Check-out"
    };

    const ICONOS_ACTIVIDAD = {
        CHECK_IN: { cls: "icon-checkin", icon: "fa-heart" },
        PASEO: { cls: "icon-walk", icon: "fa-person-walking" },
        COMIDA: { cls: "icon-food", icon: "fa-utensils" },
        DESCANSO: { cls: "icon-rest", icon: "fa-bed" },
        MEDICINA: { cls: "icon-med", icon: "fa-pills" },
        CHECK_OUT: { cls: "icon-checkout", icon: "fa-door-closed" }
    };

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
            // 1. Mostrar tarjeta de la mascota
            const primeraMascota = mascotas[0];
            petNameEl.textContent = primeraMascota.nombre;
            petStatusEl.textContent = `Raza: ${primeraMascota.raza || "Mestizo"}`;

            petCard.style.display = "flex";
            petActivitySection.style.display = "block";
            emptyPetCard.style.display = "none";

            // 2. Últimas actualizaciones reales: primera mascota con estadía
            await cargarUltimasActualizaciones(mascotas);

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

    function setLinkVerTodas(estadiaId) {
        if (estadiaId) {
            linkVerActualizaciones.href = `timeline.html?estadiaId=${estadiaId}`;
            linkVerActualizaciones.style.display = "inline";
        } else {
            linkVerActualizaciones.removeAttribute("href");
            linkVerActualizaciones.style.display = "none";
        }
    }

    async function buscarEstadiaActual(mascotas) {
        for (const m of mascotas) {
            try {
                const res = await fetch(`http://localhost:8080/api/dueno/mascotas/${m.id}/estadia-actual`, {
                    headers: { "Authorization": `Bearer ${token}` }
                });
                if (res.ok) return await res.json();
            } catch (error) {
                console.error(`Error al buscar estadía de mascota ${m.id}:`, error);
            }
        }
        return null;
    }

    async function cargarUltimasActualizaciones(mascotas) {
        try {
            const estadia = await buscarEstadiaActual(mascotas);
            if (!estadia) {
                setLinkVerTodas(null);
                renderActualizaciones([]);
                return;
            }
            setLinkVerTodas(estadia.id);

            const resActs = await fetch(`http://localhost:8080/api/dueno/estadias/${estadia.id}/actividades`, {
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (!resActs.ok) {
                renderActualizaciones([]);
                return;
            }
            const actividades = await resActs.json();
            const ultimas = (actividades || []).slice(0, 3).map(a => ({
                tipo: a.tipo,
                titulo: TITULOS_ACTIVIDAD[a.tipo] || a.tipo,
                hora: new Date(a.fechaHora).toLocaleString("es-AR", { day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit" })
            }));
            renderActualizaciones(ultimas);
        } catch (error) {
            console.error("Error al cargar actualizaciones:", error);
            setLinkVerTodas(null);
            renderActualizaciones([]);
        }
    }

    function renderActualizaciones(items) {
        if (items && items.length > 0) {
            emptyUpdates.style.display = "none";
            timelineList.style.display = "flex";
            timelineList.innerHTML = items.map(item => {
        const conf = ICONOS_ACTIVIDAD[item.tipo] || { cls: "icon-food", icon: "fa-utensils" };
        return `
        <div class="timeline-item">
          <div class="timeline-icon ${conf.cls}">
            <i class="fa-solid ${conf.icon}"></i>
          </div>
          <div class="timeline-text">
            <strong>${item.titulo}</strong>
            <small>${item.hora}</small>
          </div>
        </div>
      `}).join("");
        } else {
            emptyUpdates.style.display = "flex";
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