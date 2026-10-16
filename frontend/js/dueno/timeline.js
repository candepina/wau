document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("token");
    const userSession = JSON.parse(localStorage.getItem("userSession") || "{}");

    if (!token || userSession.rol !== "ROLE_DUENO") {
        localStorage.clear();
        window.location.href = "../index.html";
        return;
    }

    const params = new URLSearchParams(window.location.search);
    let estadiaId = params.get("estadiaId") || params.get("id");
    const mascotaId = params.get("mascotaId");

    const timelineTitle = document.getElementById("timelineTitle");
    const timelineSubtitle = document.getElementById("timelineSubtitle");
    const timelineList = document.getElementById("timelineList");
    const emptyTimeline = document.getElementById("emptyTimeline");

    const tipoConfig = {
        CHECK_IN: { icon: "fa-heart", label: "Check-in", colorClass: "check-in" },
        PASEO: { icon: "fa-person-walking", label: "Paseo", colorClass: "paseo" },
        COMIDA: { icon: "fa-utensils", label: "Comida", colorClass: "comida" },
        DESCANSO: { icon: "fa-bed", label: "Descanso", colorClass: "descanso" },
        MEDICINA: { icon: "fa-pills", label: "Medicina", colorClass: "medicina" },
        CHECK_OUT: { icon: "fa-door-closed", label: "Check-out", colorClass: "check-out" }
    };

    function authHeaders() {
        return { "Authorization": `Bearer ${token}` };
    }

    async function resolverEstadia() {
        if (estadiaId) return estadiaId;
        if (!mascotaId) {
            window.location.href = "home.html";
            return null;
        }
        const res = await fetch(`http://localhost:8080/api/dueno/mascotas/${mascotaId}/estadia-actual`, {
            headers: authHeaders()
        });
        if (res.status === 401 || res.status === 403) {
            localStorage.clear();
            window.location.href = "../index.html";
            return null;
        }
        if (!res.ok) return null;
        const estadia = await res.json();
        estadiaId = estadia.id;
        return estadiaId;
    }

    function formatearHora(fechaHoraStr) {
        return new Date(fechaHoraStr).toLocaleTimeString("es-AR", { hour: "2-digit", minute: "2-digit", hour12: true });
    }

    function nombreGrupo(fechaHoraStr) {
        const fecha = new Date(fechaHoraStr);
        const hoy = new Date();
        const ayer = new Date(hoy);
        ayer.setDate(hoy.getDate() - 1);
        const solo = (d) => new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
        if (solo(fecha) === solo(hoy)) return "hoy";
        if (solo(fecha) === solo(ayer)) return "ayer";
        return fecha.toLocaleDateString("es-AR", { day: "2-digit", month: "2-digit", year: "numeric" });
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
                    <time class="card-time">${formatearHora(actividad.fechaHora)}</time>
                </div>
                <p class="card-label">${config.label}</p>
            </div>
        </div>`;
    }

    async function cargar() {
        const id = await resolverEstadia();
        if (!id) {
            timelineList.style.display = "none";
            emptyTimeline.style.display = "block";
            return;
        }
        try {
            const res = await fetch(`http://localhost:8080/api/dueno/estadias/${id}/actividades`, {
                headers: authHeaders()
            });
            if (res.status === 401 || res.status === 403) {
                localStorage.clear();
                window.location.href = "../index.html";
                return;
            }
            if (!res.ok) throw new Error("Error al cargar actividades");
            const actividades = await res.json();
            if (actividades && actividades.length > 0) {
                emptyTimeline.style.display = "none";
                timelineList.style.display = "block";
                const grupos = {};
                for (const act of actividades) {
                    const key = nombreGrupo(act.fechaHora);
                    (grupos[key] = grupos[key] || []).push(act);
                }
                const orden = Object.keys(grupos).sort((a, b) => {
                    if (a === "hoy") return -1;
                    if (b === "hoy") return 1;
                    if (a === "ayer") return -1;
                    if (b === "ayer") return 1;
                    return b.split("/").reverse().join("-").localeCompare(a.split("/").reverse().join("-"));
                });
                timelineList.innerHTML = orden.map((key) => {
                    const titulo = key === "hoy" ? "Hoy" : key === "ayer" ? "Ayer" : key;
                    return `<div class="date-group"><div class="date-badge">${titulo}</div>${grupos[key].map(renderActividad).join("")}</div>`;
                }).join("");
            } else {
                timelineList.style.display = "none";
                emptyTimeline.style.display = "block";
            }
        } catch (error) {
            console.error("Error al cargar timeline del dueño:", error);
            timelineList.style.display = "none";
            emptyTimeline.style.display = "block";
        }
    }

    await cargar();
});
