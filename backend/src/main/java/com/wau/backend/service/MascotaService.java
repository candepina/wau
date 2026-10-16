package com.wau.backend.service;

import com.wau.backend.dto.MascotaRequest;
import com.wau.backend.dto.MascotaResponse;
import com.wau.backend.entity.Dueno;
import com.wau.backend.entity.EstadoEstadia;
import com.wau.backend.entity.Mascota;
import com.wau.backend.entity.TamanoMascota;
import com.wau.backend.entity.Usuario;
import com.wau.backend.exception.MascotaConEstadiaActivaException;
import com.wau.backend.exception.MascotaNotFoundException;
import com.wau.backend.repository.EstadiaRepository;
import com.wau.backend.repository.MascotaRepository;
import com.wau.backend.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstadiaRepository estadiaRepository;

    public MascotaService(MascotaRepository mascotaRepository, UsuarioRepository usuarioRepository, EstadiaRepository estadiaRepository) {
        this.mascotaRepository = mascotaRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadiaRepository = estadiaRepository;
    }

    public MascotaResponse registrarMascota(String emailUsuario, MascotaRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!(usuario instanceof Dueno dueno)) {
            throw new RuntimeException("Solo los usuarios con rol Dueño pueden registrar mascotas");
        }

        Mascota mascota = new Mascota();
        mascota.setCodigo(generarCodigoUnico()); // Se genera y asigna solo
        mascota.setNombre(request.getNombre());
        mascota.setRaza(request.getRaza());
        mascota.setEdad(request.getEdad());
        mascota.setTamano(parseTamano(request.getTamano()));
        mascota.setObservacionesMedicas(request.getObservacionesMedicas());
        mascota.setObservacionesConductuales(request.getObservacionesConductuales());
        mascota.setObservacionesGenerales(request.getObservacionesGenerales());
        mascota.setDueno(dueno);

        Mascota guardada = mascotaRepository.save(mascota);
        return mapearADTO(guardada);
    }

    public List<MascotaResponse> listarMascotasDelDueno(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return mascotaRepository.findByDuenoId(usuario.getId())
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    public MascotaResponse actualizarMascota(Long id, MascotaRequest request, String email) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new MascotaNotFoundException(id));

        if (!mascota.getDueno().getEmail().equals(email)) {
            throw new AccessDeniedException("No tenés permiso para modificar esta mascota.");
        }

        mascota.setNombre(request.getNombre());
        mascota.setRaza(request.getRaza());
        mascota.setEdad(request.getEdad());
        mascota.setTamano(parseTamano(request.getTamano()));
        mascota.setObservacionesMedicas(request.getObservacionesMedicas());
        mascota.setObservacionesConductuales(request.getObservacionesConductuales());
        mascota.setObservacionesGenerales(request.getObservacionesGenerales());

        return mapearADTO(mascotaRepository.save(mascota));
    }

    public void eliminarMascota(Long id, String email) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new MascotaNotFoundException(id));

        if (!mascota.getDueno().getEmail().equals(email)) {
            throw new AccessDeniedException("No tenés permiso para eliminar esta mascota.");
        }

        if (estadiaRepository.existsByMascotaIdAndEstado(id, EstadoEstadia.ACTIVA)) {
            throw new MascotaConEstadiaActivaException();
        }

        mascotaRepository.delete(mascota);
    }

    public MascotaResponse obtenerMascotaPorId(Long id, String email) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new MascotaNotFoundException(id));

        if (!mascota.getDueno().getEmail().equals(email)) {
            throw new AccessDeniedException("No tenés permiso para ver esta mascota.");
        }

        return mapearADTO(mascota);
    }

    // Método auxiliar para generar código único estilo patente
    private String generarCodigoUnico() {
        String caracteres = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Sin O, 0, I, 1 para evitar confusiones
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();

        String codigoGenerado;
        do {
            sb.setLength(0);
            for (int i = 0; i < 7; i++) {
                sb.append(caracteres.charAt(random.nextInt(caracteres.length())));
            }
            codigoGenerado = sb.toString();
        } while (mascotaRepository.existsByCodigo(codigoGenerado));

        return codigoGenerado;
    }

    private TamanoMascota parseTamano(String tamano) {
        if (tamano == null || tamano.isBlank()) {
            return null;
        }
        try {
            return TamanoMascota.valueOf(tamano.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tamaño de mascota inválido: " + tamano + ". Valores válidos: PEQUENO, MEDIANO, GRANDE");
        }
    }


    private MascotaResponse mapearADTO(Mascota mascota) {
        return new MascotaResponse(
                mascota.getId(),
                mascota.getCodigo(),
                mascota.getNombre(),
                mascota.getRaza(),
                mascota.getEdad(),
                mascota.getTamano() != null ? mascota.getTamano().name() : null,
                mascota.getObservacionesMedicas(),
                mascota.getObservacionesConductuales(),
                mascota.getObservacionesGenerales(),
                mascota.getDueno().getId()
        );
    }
}