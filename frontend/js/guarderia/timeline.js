document.addEventListener("DOMContentLoaded", async () => {
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

    // Elementos del DOM
    const timelineTitle = document.getElementById("timelineTitle");
    const timelineList = document.getElementById("timelineList");
    const emptyTimeline = document.getElementById("emptyTimeline");
    const btnNuevaActividadHeader = document.getElementById("btnNuevaActividadHeader");
    const fabNuevaActividad = document.getElementById("fabNuevaActividad");
    const btnPrimeraActividad = document.getElementById("btnPrimeraActividad");

    // Configurar hrefs de los botones de nueva actividad
    const nuevaActividadUrl = `nueva-actividad.html?id=${estadiaId}`;
    if (btnNuevaActividadHeader) btnNuevaActividadHeader.href = nuevaActividadUrl;
    if (fabNuevaActividad) fabNuevaActividad.href = nuevaActividadUrl;
    if (btnPrimeraActividad) btnPrimeraActividad.href = nuevaActividadUrl;

    // Mapeo de tipos a iconos, labels y colorClass
    const tipoConfig = {
        CHECK_IN: { icon: "fa-heart", label: "Check-in", colorClass: "check-in" },
        PASEO: { icon: "fa-person-walking", label: "Paseo", colorClass: "paseo" },
        COMIDA: { icon: "fa-utensils", label: "Comida", colorClass: "comida" },
        DESCANSO: { icon: "fa-bed", label: "Descanso", colorClass: "descanso" },
        MEDICINA: { icon: "fa-pills", label: "Medicina", colorClass: "medicina" },
        CHECK_OUT: { icon: "fa-door-closed", label: "Check-out", colorClass: "check-out" }
    };

    function formatearFechaHora(fechaHoraStr) {
        const fecha = new Date(fechaHoraStr);
        return fecha.toLocaleTimeString("es-AR", { hour: "2-digit", minute: "2-digit", hour12: true });
    }

    function formatearFecha(fechaHoraStr) {
        const fecha = new Date(fechaHoraStr);
        const hoy = new Date();
        const ayer = new Date(hoy);
        ayer.setDate(hoy.getDate() - 1);

        const fechaSolo = new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate());
        const hoySolo = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
        const ayerSolo = new Date(ayer.getFullYear(), ayer.getMonth(), ayer.getDate());

        if (fechaSolo.getTime() === hoySolo.getTime()) {
            return "Hoy";
        } else if (fechaSolo.getTime() === ayerSolo.getTime()) {
            return "Ayer";
        } else {
            return fecha.toLocaleDateString("es-AR", { day: "2-digit", month: "2-digit", year: "numeric" });
        }
    }

    function agruparPorFecha(actividades) {
        const grupos = {};

        for (const act of actividades) {
            const fecha = new Date(act.fechaHora);
            const hoy = new Date();
            const ayer = new Date(hoy);
            ayer.setDate(hoy.getDate() - 1);

            const fechaSolo = new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate());
            const hoySolo = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
            const ayerSolo = new Date(ayer.getFullYear(), ayer.getMonth(), ayer.getDate());

            let key;
            if (fechaSolo.getTime() === hoySolo.getTime()) {
                key = "hoy";
            } else if (fechaSolo.getTime() === ayerSolo.getTime()) {
                key = "ayer";
            } else {
                key = fecha.toLocaleDateString("es-AR", { day: "2-digit", month: "2-digit", year: "numeric" });
            }

            if (!grupos[key]) {
                grupos[key] = [];
            }
            grupos[key].push(act);
        }

        // Ordenar grupos: Hoy > Ayer > fechas descendentes
        const orden = Object.keys(grupos).sort((a, b) => {
            if (a === "hoy") return -1;
            if (b === "hoy") return 1;
            if (a === "ayer") return -1;
            if (b === "ayer") return 1;
            const fechaA = a.split("/").reverse().join("-");
            const fechaB = b.split("/").reverse().join("-");
            return fechaB.localeCompare(fechaA);
        });

        const gruposOrdenados = {};
        for (const key of orden) {
            gruposOrdenados[key] = grupos[key];
        }

        return gruposOrdenados;
    }

    function renderActividad(actividad) {
        const config = tipoConfig[actividad.tipo] || { icon: "fa-circle", label: actividad.tipo, colorClass: "" };
        return `
        <div class="timeline-item">
            <div class="timeline-node ${config.colorClass}">
                <i class="fa-solid ${config.icon}"></i>
            </div>
            <div class="timeline-card">
                <div class="card-header">
                    <span class="card-type ${config.colorClass}">
                        <i class="fa-solid ${config.icon}"></i>
                        ${config.label}
                    </span>
                    <time class="card-time">${formatearFechaHora(actividad.fechaHora)}</time>
                </div>
                <p class="card-label">${config.label}</p>
            </div>
        </div>
        `;
    }

    function crearDateBadge(fechaHoraStr) {
        const texto = formatearFecha(fechaHoraStr);
        return `<div class="date-badge">${texto}</div>`;
    }

    function formatearFechaHora(fechaHoraStr) {
        const fecha = new Date(fechaHoraStr);
        return fecha.toLocaleTimeString("es-AR", { hour: "2-digit", minute: "2-digit", hour12: true });
    }

    function formatearFecha(fechaHoraStr) {
        const fecha = new Date(fechaHoraStr);
        const hoy = new Date();
        const ayer = new Date(hoy);
        ayer.setDate(hoy.getDate() - 1);

        const fechaSolo = new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate());
        const hoySolo = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
        const ayerSolo = new Date(ayer.getFullYear(), ayer.getMonth(), ayer.getDate());

        if (fechaSolo.getTime() === hoySolo.getTime()) {
            return "Hoy";
        } else if (fechaSolo.getTime() === ayerSolo.getTime()) {
            return "Ayer";
        } else {
            return fecha.toLocaleDateString("es-AR", { day: "2-digit", month: "2-digit", year: "numeric" });
        }
    }

    function agruparPorFecha(actividades) {
        const grupos = {};

        for (const act of actividades) {
            const fecha = new Date(act.fechaHora);
            const hoy = new Date();
            const ayer = new Date(hoy);
            ayer.setDate(hoy.getDate() - 1);

            const fechaSolo = new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate());
            const hoySolo = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
            const ayerSolo = new Date(ayer.getFullYear(), ayer.getMonth(), ayer.getDate());

            let key;
            if (fechaSolo.getTime() === hoySolo.getTime()) {
                key = "hoy";
            } else if (fechaSolo.getTime() === ayerSolo.getTime()) {
                key = "ayer";
            } else {
                key = fecha.toLocaleDateString("es-AR", { day: "2-digit", month: "2-digit", year: "numeric" });
            }

            if (!grupos[key]) {
                grupos[key] = [];
            }
            grupos[key].push(act);
        }

        // Ordenar grupos: Hoy > Ayer > fechas descendentes
        const orden = Object.keys(grupos).sort((a, b) => {
            if (a === "hoy") return -1;
            if (b === "hoy") return 1;
            if (a === "ayer") return -1;
            if (b === "ayer") return 1;
            const fechaA = a.split("/").reverse().join("-");
            const fechaB = b.split("/").reverse().join("-");
            return fechaB.localeCompare(fechaA);
        });

        const gruposOrdenados = {};
        for (const key of orden) {
            gruposOrdenados[key] = grupos[key];
        }

        return gruposOrdenados;
    }

    function crearDateBadge(fechaHoraStr) {
        const texto = formatearFecha(fechaHoraStr);
        return `<div class="date-badge">${texto}</div>`;
    }

    function formatearFechaHora(fechaHoraStr) {
        const fecha = new Date(fechaHoraStr);
        return fecha.toLocaleTimeString("es-AR", { hour: "2-digit", minute: "2-digit", hour12: true });
    }

    function formatearFecha(fechaHoraStr) {
        const fecha = new Date(fechaHoraStr);
        const hoy = new Date();
        const ayer = new Date(hoy);
        ayer.setDate(hoy.getDate() - 1);

        const fechaSolo = new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate());
        const hoySolo = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
        const ayerSolo = new Date(ayer.getFullYear(), ayer.getMonth(), ayer.getDate());

        if (fechaSolo.getTime() === hoySolo.getTime()) {
            return "Hoy";
        } else if (fechaSolo.getTime() === ayerSolo.getTime()) {
            return "Ayer";
        } else {
            return fecha.toLocaleDateString("es-AR", { day: "2-digit", month: "2-digit", year: "numeric" });
        }
    }

    async function cargarActividades() {
        try {
            const response = await fetch(API, {
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
                throw new Error("Error al cargar actividades");
            }

            const actividades = await response.json();

            if (actividades && actividades.length > 0) {
                emptyTimeline.style.display = "none";
                timelineList.style.display = "block";

                const grupos = agruparPorFecha(actividades);

                // Renderizar: cada grupo envuelto en .date-group con date-badge ANTES de los items
                timelineList.innerHTML = Object.entries(grupos).map(([fechaKey, acts]) => {
                    const dateBadge = crearDateBadge(acts[0].fechaHora);
                    const itemsHtml = acts.map(renderActividad).join("");
                    return `<div class="date-group">${dateBadge}${itemsHtml}</div>`;
                }).join("");

            } else {
                timelineList.style.display = "none";
                document.getElementById("emptyTimeline").style.display = "block";
            }

        } catch (error) {
            console.error("Error al cargar actividades:", error);
            timelineList.style.display = "none";
            document.getElementById("emptyTimeline").style.display = "block";
        }
    }

    async function cargarInfoEstadia() {
        try {
            const response = await fetch(`http://localhost:8080/api/estadias/${estadiaId}`, {
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            });

            if (response.ok) {
                const estadia = await response.json();
                timelineTitle.textContent = "Actualizaciones";
            }
        } catch (error) {
            console.error("Error al cargar info de estadía:", error);
        }
    }

    // Mapeo de tipos (global para acceso en funciones)
    window.tipoConfig = {
        CHECK_IN: { icon: "fa-heart", label: "Check-in", colorClass: "check-in" },
        PASEO: { icon: "fa-person-walking", label: "Paseo", colorClass: "paseo" },
        COMIDA: { icon: "fa-utensils", label: "Comida", colorClass: "comida" },
        DESCANSO: { icon: "fa-bed", label: "Descanso", colorClass: "descanso" },
        MEDICINA: { icon: "fa-pills", label: "Medicina", colorClass: "medicina" },
        CHECK_OUT: { icon: "fa-door-closed", label: "Check-out", colorClass: "check-out" }
    };

    // Cargar datos
    await Promise.all([cargarActividades(), cargarInfoEstadia()]);
});