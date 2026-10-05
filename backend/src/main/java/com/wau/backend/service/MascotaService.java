package com.wau.backend.service;

import com.wau.backend.dto.MascotaRequest;
import com.wau.backend.dto.MascotaResponse;
import com.wau.backend.entity.Dueno;
import com.wau.backend.entity.Mascota;
import com.wau.backend.entity.Usuario;
import com.wau.backend.repository.MascotaRepository;
import com.wau.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    public MascotaService(MascotaRepository mascotaRepository, UsuarioRepository usuarioRepository) {
        this.mascotaRepository = mascotaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public MascotaResponse registrarMascota(String emailUsuario, MascotaRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!(usuario instanceof Dueno dueno)) {
            throw new RuntimeException("Solo los usuarios con rol Dueño pueden registrar mascotas");
        }

        Mascota mascota = new Mascota();
        mascota.setNombre(request.getNombre());
        mascota.setRaza(request.getRaza());
        mascota.setEdad(request.getEdad());
        mascota.setTamano(request.getTamano());
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

    private MascotaResponse mapearADTO(Mascota mascota) {
        return new MascotaResponse(
                mascota.getId(),
                mascota.getNombre(),
                mascota.getRaza(),
                mascota.getEdad(),
                mascota.getTamano(),
                mascota.getObservacionesMedicas(),
                mascota.getObservacionesConductuales(),
                mascota.getObservacionesGenerales(),
                mascota.getDueno().getId()
        );
    }
}