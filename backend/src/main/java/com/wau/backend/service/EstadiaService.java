package com.wau.backend.service;

import com.wau.backend.dto.EstadiaRequest;
import com.wau.backend.dto.EstadiaResponse;
import com.wau.backend.entity.Estadia;
import com.wau.backend.entity.Guarderia;
import com.wau.backend.entity.Mascota;
import com.wau.backend.entity.Usuario;
import com.wau.backend.repository.EstadiaRepository;
import com.wau.backend.repository.MascotaRepository;
import com.wau.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstadiaService {

    private final EstadiaRepository estadiaRepository;
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    public EstadiaService(EstadiaRepository estadiaRepository,
                          MascotaRepository mascotaRepository,
                          UsuarioRepository usuarioRepository) {
        this.estadiaRepository = estadiaRepository;
        this.mascotaRepository = mascotaRepository;
        this.usuarioRepository = usuarioRepository;
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