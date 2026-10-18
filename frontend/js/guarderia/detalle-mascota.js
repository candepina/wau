// Ficha de mascota para la guardería (solo lectura)
const token = localStorage.getItem("token");
const userSession = JSON.parse(localStorage.getItem("userSession") || "{}");

if (!token || userSession.rol !== "ROLE_GUARDERIA") {
    localStorage.clear();
    window.location.href = "../index.html";
}

// El dueño guarda salud como "Alergias: X | Medicación: Y" (ver dueno/detalle-mascota.js).
// Acá lo volvemos a separar para mostrar cada cosa en su campo.
function separarObservacionesMedicas(raw) {
    if (!raw) return { alergias: "", medicacion: "" };

    const partes = raw.split("|").map(p => p.trim()).filter(Boolean);
    const alergias = [];
    const medicacion = [];
    const sueltos = [];

    for (const parte of partes) {
        if (/^alergias\s*:/i.test(parte)) {
            alergias.push(parte.replace(/^alergias\s*:\s*/i, ""));
        } else if (/^medicaci[oó]n\s*:/i.test(parte)) {
            medicacion.push(parte.replace(/^medicaci[oó]n\s*:\s*/i, ""));
        } else {
            sueltos.push(parte);
        }
    }

    return {
        alergias: [...alergias, ...sueltos].join(" | "),
        medicacion: medicacion.join(" | ")
    };
}

function mostrarFeedback(msg) {
    const feedback = document.getElementById("formFeedback");
    feedback.textContent = msg;
    feedback.className = "feedback-msg error";
    feedback.style.display = "block";
}

async function cargarFicha() {
    const mascotaId = new URLSearchParams(window.location.search).get("id");
    if (!mascotaId) {
        window.location.href = "home.html";
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/api/mascotas/${mascotaId}/ficha`, {
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (response.status === 401) {
            localStorage.clear();
            window.location.href = "../index.html";
            return;
        }

        if (response.status === 403 || response.status === 404) {
            mostrarFeedback("No se pudo abrir la ficha de esta mascota.");
            return;
        }

        if (!response.ok) {
            throw new Error("No se pudo cargar la ficha de la mascota.");
        }

        const ficha = await response.json();
        pintarFicha(ficha);
    } catch (error) {
        console.error(error);
        mostrarFeedback("No se pudo cargar la ficha. Verificá tu conexión e intentá de nuevo.");
    }
}

function pintarFicha(ficha) {
    const salud = separarObservacionesMedicas(ficha.observacionesMedicas);

    document.getElementById("nombre").value = ficha.nombre || "";
    document.getElementById("codigo").value = ficha.codigo || "";
    document.getElementById("raza").value = ficha.raza || "Mestizo";
    document.getElementById("edad").value = ficha.edad ?? "";
    document.getElementById("tamano").value = traducirTamano(ficha.tamano);
    document.getElementById("alimentacion").value = ficha.observacionesGenerales || "";
    document.getElementById("alergias").value = salud.alergias;
    document.getElementById("medicacion").value = salud.medicacion;
    document.getElementById("conductuales").value = ficha.observacionesConductuales || "";

    // Contacto del dueño
    document.getElementById("duenoNombre").textContent = ficha.duenoNombre || "—";
    const telefono = ficha.duenoTelefono || "";
    document.getElementById("duenoTelefono").textContent = telefono || "—";

    if (telefono) {
        document.getElementById("btnLlamar").href = `tel:${telefono}`;
        const soloDigitos = telefono.replace(/\D/g, "");
        document.getElementById("btnWhatsapp").href = `https://wa.me/${soloDigitos}`;
    } else {
        document.getElementById("btnLlamar").style.display = "none";
        document.getElementById("btnWhatsapp").style.display = "none";
    }

    document.getElementById("avatarContainer").style.display = "flex";
    document.getElementById("fichaMascotaForm").style.display = "flex";
}

function traducirTamano(tamano) {
    const traducciones = { PEQUENO: "Pequeño", MEDIANO: "Mediano", GRANDE: "Grande" };
    return traducciones[tamano] || "";
}

document.addEventListener("DOMContentLoaded", cargarFicha);
