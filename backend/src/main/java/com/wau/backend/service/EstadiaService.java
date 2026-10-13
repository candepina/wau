package com.wau.backend.service;

import com.wau.backend.dto.ActividadRequest;
import com.wau.backend.dto.ActividadResponse;
import com.wau.backend.dto.EstadiaRequest;
import com.wau.backend.dto.EstadiaResponse;
import com.wau.backend.entity.Actividad;
import com.wau.backend.entity.Estadia;
import com.wau.backend.entity.Guarderia;
import com.wau.backend.entity.Mascota;
import com.wau.backend.entity.TipoActividad;
import com.wau.backend.entity.Usuario;
import com.wau.backend.repository.ActividadRepository;
import com.wau.backend.repository.EstadiaRepository;
import com.wau.backend.repository.MascotaRepository;
import com.wau.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstadiaService {

    private final EstadiaRepository estadiaRepository;
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ActividadRepository actividadRepository;

    public EstadiaService(EstadiaRepository estadiaRepository,
                          MascotaRepository mascotaRepository,
                          UsuarioRepository usuarioRepository,
                          ActividadRepository actividadRepository) {
        this.estadiaRepository = estadiaRepository;
        this.mascotaRepository = mascotaRepository;
        this.usuarioRepository = usuarioRepository;
        this.actividadRepository = actividadRepository;
    }

    public EstadiaResponse crearEstadia(String emailGuarderia, EstadiaRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(emailGuarderia)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!(usuario instanceof Guarderia guarderia)) {
            throw new RuntimeException("Solo usuarios con rol Guardería pueden crear estadías");
        }

        // Buscamos por el código patente en vez del ID numérico
        Mascota mascota = mascotaRepository.findByCodigo(request.getCodigoMascota().trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("No se encontró ninguna mascota con el código: " + request.getCodigoMascota()));

        Estadia estadia = new Estadia();
        estadia.setMascota(mascota);
        estadia.setGuarderia(guarderia);
        estadia.setFechaInicio(request.getFechaInicio());
        estadia.setFechaFin(request.getFechaFin());
        estadia.setNotasIngreso(request.getNotasIngreso());
        estadia.setEstado("ACTIVA");

        Estadia guardada = estadiaRepository.save(estadia);
        return mapearADTO(guardada);
    }

    public List<EstadiaResponse> listarEstadiasActivas(String emailGuarderia) {
        Usuario usuario = usuarioRepository.findByEmail(emailGuarderia)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return estadiaRepository.findByGuarderiaIdAndEstado(usuario.getId(), "ACTIVA")
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    public List<ActividadResponse> listarActividades(Long estadiaId, String emailGuarderia) {
        Estadia estadia = estadiaRepository.findById(estadiaId)
                .orElseThrow(() -> new RuntimeException("Estadía no encontrada con ID: " + estadiaId));

        validarPertenenciaGuarderia(estadia, emailGuarderia);

        return actividadRepository.findByEstadiaIdOrderByFechaHoraDesc(estadiaId)
                .stream()
                .map(this::mapearActividadADTO)
                .collect(Collectors.toList());
    }

    public ActividadResponse agregarActividad(Long estadiaId, ActividadRequest request, String emailGuarderia) {
        Estadia estadia = estadiaRepository.findById(estadiaId)
                .orElseThrow(() -> new RuntimeException("Estadía no encontrada con ID: " + estadiaId));

        validarPertenenciaGuarderia(estadia, emailGuarderia);

        // Validar CHECK_IN y CHECK_OUT únicos por estadía
        if (request.getTipo() == TipoActividad.CHECK_IN) {
            if (actividadRepository.existsByEstadiaIdAndTipo(estadiaId, TipoActividad.CHECK_IN)) {
                throw new RuntimeException("Ya existe un check-in para esta estadía");
            }
        }
        if (request.getTipo() == TipoActividad.CHECK_OUT) {
            if (actividadRepository.existsByEstadiaIdAndTipo(estadiaId, TipoActividad.CHECK_OUT)) {
                throw new RuntimeException("Ya existe un check-out para esta estadía");
            }
            if (!actividadRepository.existsByEstadiaIdAndTipo(estadiaId, TipoActividad.CHECK_IN)) {
                throw new RuntimeException("No se puede hacer check-out sin un check-in previo");
            }
        }

        Actividad actividad = new Actividad();
        actividad.setTipo(request.getTipo());
        actividad.setFechaHora(LocalDateTime.now());
        actividad.setEstadia(estadia);

        Actividad guardada = actividadRepository.save(actividad);
        return mapearActividadADTO(guardada);
    }

    private void validarPertenenciaGuarderia(Estadia estadia, String emailGuarderia) {
        if (!estadia.getGuarderia().getEmail().equals(emailGuarderia)) {
            throw new RuntimeException("No tenés permiso para acceder a esta estadía");
        }
    }

    public EstadiaResponse obtenerEstadiaPorId(Long estadiaId, String emailGuarderia) {
        Estadia estadia = estadiaRepository.findById(estadiaId)
                .orElseThrow(() -> new RuntimeException("Estadía no encontrada con ID: " + estadiaId));

        validarPertenenciaGuarderia(estadia, emailGuarderia);

        return mapearADTO(estadia);
    }

    private EstadiaResponse mapearADTO(Estadia estadia) {
        return new EstadiaResponse(
                estadia.getId(),
                estadia.getMascota().getId(),
                estadia.getMascota().getNombre(),
                estadia.getMascota().getRaza(),
                estadia.getMascota().getDueno().getNombre(),
                estadia.getFechaInicio(),
                estadia.getFechaFin(),
                estadia.getEstado(),
                estadia.getNotasIngreso()
        );
    }

    private ActividadResponse mapearActividadADTO(Actividad actividad) {
        return new ActividadResponse(
                actividad.getId(),
                actividad.getTipo(),
                actividad.getFechaHora()
        );
    }

    public EstadiaResponse finalizarEstadia(String emailGuarderia, Long estadiaId) {
        Usuario usuario = usuarioRepository.findByEmail(emailGuarderia)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!(usuario instanceof Guarderia guarderia)) {
            throw new RuntimeException("Solo la guardería puede finalizar estadías");
        }

        Estadia estadia = estadiaRepository.findById(estadiaId)
                .orElseThrow(() -> new RuntimeException("Estadía no encontrada con ID: " + estadiaId));

        // Validación de pertenencia
        if (!estadia.getGuarderia().getId().equals(guarderia.getId())) {
            throw new RuntimeException("No tenés permiso para modificar esta estadía");
        }

        if ("FINALIZADA".equalsIgnoreCase(estadia.getEstado())) {
            throw new RuntimeException("Esta estadía ya se encuentra finalizada");
        }

        estadia.setEstado("FINALIZADA");
        estadia.setFechaFin(java.time.LocalDate.now()); // Registra la fecha de egreso efectiva

        Estadia guardada = estadiaRepository.save(estadia);
        return mapearADTO(guardada);
    }
}