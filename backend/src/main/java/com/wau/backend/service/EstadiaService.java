package com.wau.backend.service;

import com.wau.backend.dto.ActividadRequest;
import com.wau.backend.dto.ActividadResponse;
import com.wau.backend.dto.EstadiaRequest;
import com.wau.backend.dto.EstadiaResponse;
import com.wau.backend.entity.Actividad;
import com.wau.backend.entity.Estadia;
import com.wau.backend.entity.EstadoEstadia;
import com.wau.backend.entity.Guarderia;
import com.wau.backend.entity.Mascota;
import com.wau.backend.entity.TipoActividad;
import com.wau.backend.entity.Usuario;
import com.wau.backend.repository.ActividadRepository;
import com.wau.backend.repository.EstadiaRepository;
import com.wau.backend.repository.MascotaRepository;
import com.wau.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        if (request.getFechaInicio() != null && request.getFechaInicio().isAfter(LocalDate.now())) {
            estadia.setEstado(EstadoEstadia.PROGRAMADA);
        } else {
            estadia.setEstado(EstadoEstadia.ACTIVA);
        }

        Estadia guardada = estadiaRepository.save(estadia);
        return mapearADTO(guardada);
    }

    public List<EstadiaResponse> listarEstadiasActivas(String emailGuarderia) {
        Usuario usuario = usuarioRepository.findByEmail(emailGuarderia)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        promoverVencidasGuarderia(usuario.getId());

        return estadiaRepository.findByGuarderiaIdAndEstado(usuario.getId(), EstadoEstadia.ACTIVA)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    public List<EstadiaResponse> listarMisEstadias(String emailGuarderia) {
        Usuario usuario = usuarioRepository.findByEmail(emailGuarderia)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        promoverVencidasGuarderia(usuario.getId());

        List<EstadiaResponse> activas = estadiaRepository
                .findByGuarderiaIdAndEstado(usuario.getId(), EstadoEstadia.ACTIVA)
                .stream().map(this::mapearADTO).collect(Collectors.toList());
        List<EstadiaResponse> programadas = estadiaRepository
                .findByGuarderiaIdAndEstado(usuario.getId(), EstadoEstadia.PROGRAMADA)
                .stream().map(this::mapearADTO).collect(Collectors.toList());

        activas.sort((a, b) -> {
            if (a.getFechaInicio() == null) return 1;
            if (b.getFechaInicio() == null) return -1;
            return a.getFechaInicio().compareTo(b.getFechaInicio());
        });
        programadas.sort((a, b) -> {
            if (a.getFechaInicio() == null) return 1;
            if (b.getFechaInicio() == null) return -1;
            return a.getFechaInicio().compareTo(b.getFechaInicio());
        });

        activas.addAll(programadas);
        return activas;
    }

    public List<ActividadResponse> listarActividades(Long estadiaId, String emailGuarderia) {
        Estadia estadia = estadiaRepository.findById(estadiaId)
                .orElseThrow(() -> new RuntimeException("Estadía no encontrada con ID: " + estadiaId));

        validarPertenenciaGuarderia(estadia, emailGuarderia);
        estadia = promoverSiVencida(estadia);

        return actividadRepository.findByEstadiaIdOrderByFechaHoraDesc(estadiaId)
                .stream()
                .map(this::mapearActividadADTO)
                .collect(Collectors.toList());
    }

    public ActividadResponse agregarActividad(Long estadiaId, ActividadRequest request, String emailGuarderia) {
        Estadia estadia = estadiaRepository.findById(estadiaId)
                .orElseThrow(() -> new RuntimeException("Estadía no encontrada con ID: " + estadiaId));

        validarPertenenciaGuarderia(estadia, emailGuarderia);
        estadia = promoverSiVencida(estadia);

        // El CHECK_IN marca la apertura real aunque la fecha planificada sea futura
        if (request.getTipo() == TipoActividad.CHECK_IN && estadia.estaProgramada()) {
            estadia.activar();
            estadia = estadiaRepository.save(estadia);
        }

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

    private Estadia promoverSiVencida(Estadia estadia) {
        if (estadia.estaProgramada()
                && estadia.getFechaInicio() != null
                && !estadia.getFechaInicio().isAfter(LocalDate.now())) {
            estadia.activar();
            return estadiaRepository.save(estadia);
        }
        return estadia;
    }

    private void promoverVencidasGuarderia(Long guarderiaId) {
        List<Estadia> vencidas = estadiaRepository
                .findByGuarderiaIdAndEstadoAndFechaInicioLessThanEqual(
                        guarderiaId, EstadoEstadia.PROGRAMADA, LocalDate.now());
        for (Estadia e : vencidas) {
            e.activar();
        }
        if (!vencidas.isEmpty()) {
            estadiaRepository.saveAll(vencidas);
        }
    }

    public EstadiaResponse obtenerEstadiaPorId(Long estadiaId, String emailGuarderia) {
        Estadia estadia = estadiaRepository.findById(estadiaId)
                .orElseThrow(() -> new RuntimeException("Estadía no encontrada con ID: " + estadiaId));

        validarPertenenciaGuarderia(estadia, emailGuarderia);
        estadia = promoverSiVencida(estadia);

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
                estadia.getEstado() != null ? estadia.getEstado().name() : null,
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

        estadia = promoverSiVencida(estadia);
        estadia.finalizar();

        Estadia guardada = estadiaRepository.save(estadia);
        return mapearADTO(guardada);
    }

    public EstadiaResponse actualizarEstadia(Long estadiaId, EstadiaRequest request, String emailGuarderia) {
        Estadia estadia = estadiaRepository.findById(estadiaId)
                .orElseThrow(() -> new RuntimeException("Estadía no encontrada con ID: " + estadiaId));

        validarPertenenciaGuarderia(estadia, emailGuarderia);
        estadia = promoverSiVencida(estadia);

        if (estadia.getEstado() == EstadoEstadia.FINALIZADA) {
            throw new RuntimeException("No se puede modificar una estadía finalizada");
        }

        if (request.getFechaInicio() != null) {
            estadia.setFechaInicio(request.getFechaInicio());
        }
        if (request.getFechaFin() != null) {
            estadia.setFechaFin(request.getFechaFin());
        }
        if (request.getNotasIngreso() != null) {
            estadia.setNotasIngreso(request.getNotasIngreso());
        }

        Estadia guardada = estadiaRepository.save(estadia);
        return mapearADTO(guardada);
    }

    private void validarPertenenciaDueno(Estadia estadia, String emailDueno) {
        if (estadia.getMascota() == null
                || estadia.getMascota().getDueno() == null
                || !estadia.getMascota().getDueno().getEmail().equals(emailDueno)) {
            throw new RuntimeException("No tenés permiso para ver esta estadía");
        }
    }

    public EstadiaResponse obtenerEstadiaActualDeMiMascota(Long mascotaId, String emailDueno) {
        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada con ID: " + mascotaId));

        if (mascota.getDueno() == null || !mascota.getDueno().getEmail().equals(emailDueno)) {
            throw new RuntimeException("No tenés permiso para ver esta mascota");
        }

        return estadiaRepository.findFirstByMascotaIdAndEstadoOrderByFechaInicioAsc(mascotaId, EstadoEstadia.ACTIVA)
                .map(e -> mapearADTO(promoverSiVencida(e)))
                .or(() -> estadiaRepository.findFirstByMascotaIdAndEstadoOrderByFechaInicioAsc(mascotaId, EstadoEstadia.PROGRAMADA)
                        .map(e -> mapearADTO(promoverSiVencida(e))))
                .orElseThrow(() -> new RuntimeException("No hay estadía activa para esta mascota"));
    }

    public java.util.List<ActividadResponse> listarActividadesParaDueno(Long estadiaId, String emailDueno) {
        Estadia estadia = estadiaRepository.findById(estadiaId)
                .orElseThrow(() -> new RuntimeException("Estadía no encontrada con ID: " + estadiaId));

        validarPertenenciaDueno(estadia, emailDueno);
        promoverSiVencida(estadia);

        return actividadRepository.findByEstadiaIdOrderByFechaHoraDesc(estadiaId)
                .stream()
                .map(this::mapearActividadADTO)
                .collect(Collectors.toList());
    }
}