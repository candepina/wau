package com.wau.backend.service;

import com.wau.backend.dto.PerfilRequest;
import com.wau.backend.dto.PerfilResponse;
import com.wau.backend.entity.Dueno;
import com.wau.backend.entity.EstadoEstadia;
import com.wau.backend.entity.Guarderia;
import com.wau.backend.entity.Mascota;
import com.wau.backend.entity.Usuario;
import com.wau.backend.exception.UsuarioConEstadiasActivasException;
import com.wau.backend.exception.UsuarioNotFoundException;
import com.wau.backend.repository.DuenoRepository;
import com.wau.backend.repository.EstadiaRepository;
import com.wau.backend.repository.GuarderiaRepository;
import com.wau.backend.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final DuenoRepository duenoRepository;
    private final GuarderiaRepository guarderiaRepository;
    private final EstadiaRepository estadiaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          DuenoRepository duenoRepository,
                          GuarderiaRepository guarderiaRepository,
                          EstadiaRepository estadiaRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.duenoRepository = duenoRepository;
        this.guarderiaRepository = guarderiaRepository;
        this.estadiaRepository = estadiaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PerfilResponse obtenerPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNotFoundException(email));
        return mapToResponse(usuario);
    }

    @Transactional
    public PerfilResponse actualizarPerfil(String email, PerfilRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNotFoundException(email));

        // Validar password actual si se quiere cambiar
        if (request.getPasswordNuevo() != null && !request.getPasswordNuevo().isBlank()) {
            if (request.getPasswordActual() == null || request.getPasswordActual().isBlank()) {
                throw new IllegalArgumentException("Debes ingresar tu contraseña actual para cambiarla");
            }
            if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
                throw new IllegalArgumentException("La contraseña actual es incorrecta");
            }
            if (request.getPasswordNuevoConfirmacion() == null || !request.getPasswordNuevo().equals(request.getPasswordNuevoConfirmacion())) {
                throw new IllegalArgumentException("Las nuevas contraseñas no coinciden");
            }
        }

        // Actualizar campos según rol
        if (usuario instanceof Dueno dueno) {
            actualizarDueno(dueno, request);
            usuario = duenoRepository.save(dueno);
        } else if (usuario instanceof Guarderia guarderia) {
            actualizarGuarderia(guarderia, request);
            usuario = guarderiaRepository.save(guarderia);
        }

        // Actualizar teléfono (campo común)
        usuario.setTelefono(request.getTelefono());

        // Cambiar password si se proporcionó
        if (request.getPasswordNuevo() != null && !request.getPasswordNuevo().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPasswordNuevo()));
            usuario = usuarioRepository.save(usuario);
        }

        return mapToResponse(usuario);
    }

    @Transactional
    public void eliminarCuenta(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNotFoundException(email));

        if (usuario instanceof Dueno dueno) {
            // Validar: NINGUNA mascota del dueño tiene estadía ACTIVA
            if (mascotasDeDuenoConEstadiaActiva(dueno.getId())) {
                throw new UsuarioConEstadiasActivasException(
                    "No se puede eliminar la cuenta porque una o más de tus mascotas tienen una estadía activa"
                );
            }
            // El cascade + orphanRemoval se encarga de borrar las mascotas
        } else if (usuario instanceof Guarderia guarderia) {
            // Validar: NO hay estadías ACTIVAS en esta guardería
            if (estadiaRepository.existsByGuarderiaIdAndEstado(guarderia.getId(), EstadoEstadia.ACTIVA)) {
                throw new UsuarioConEstadiasActivasException(
                    "No se puede eliminar la cuenta porque la guardería tiene huéspedes activos"
                );
            }
        }

        usuarioRepository.delete(usuario);
    }

    private boolean mascotasDeDuenoConEstadiaActiva(Long duenoId) {
        // Buscar si existe alguna estadía ACTIVA donde la mascota pertenece al dueño
        return estadiaRepository.existsByMascotaDuenoIdAndEstado(duenoId, EstadoEstadia.ACTIVA);
    }

    private void actualizarDueno(Dueno dueno, PerfilRequest request) {
        validarCampoObligatorio(request.getNombre(), "El nombre es obligatorio");
        validarCampoObligatorio(request.getApellido(), "El apellido es obligatorio");
        validarCampoObligatorio(request.getDni(), "El DNI es obligatorio");
        validarFormatoDni(request.getDni());
        validarCampoObligatorio(request.getDireccionDomicilio(), "La dirección es obligatoria");

        dueno.setNombre(request.getNombre());
        dueno.setApellido(request.getApellido());
        dueno.setDni(request.getDni());
        dueno.setDireccionDomicilio(request.getDireccionDomicilio());
    }

    private void actualizarGuarderia(Guarderia guarderia, PerfilRequest request) {
        validarCampoObligatorio(request.getNombreEstablecimiento(), "El nombre del establecimiento es obligatorio");
        validarCampoObligatorio(request.getNombreResponsable(), "El nombre del responsable es obligatorio");
        validarCampoObligatorio(request.getDireccionPredio(), "La dirección del predio es obligatoria");

        guarderia.setNombreEstablecimiento(request.getNombreEstablecimiento());
        guarderia.setNombreResponsable(request.getNombreResponsable());
        guarderia.setDireccionPredio(request.getDireccionPredio());
    }

    private void validarCampoObligatorio(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private void validarFormatoDni(String dni) {
        if (dni != null && !dni.matches("^[0-9.\\-\\s]{7,15}$")) {
            throw new IllegalArgumentException("Formato de DNI inválido");
        }
    }

    private PerfilResponse mapToResponse(Usuario usuario) {
        PerfilResponse response = new PerfilResponse();
        response.setId(usuario.getId());
        response.setEmail(usuario.getEmail());
        response.setTelefono(usuario.getTelefono());
        response.setRol(usuario.getRol());
        response.setFechaAlta(usuario.getFechaAlta());

        if (usuario instanceof Dueno dueno) {
            response.setNombre(dueno.getNombre());
            response.setApellido(dueno.getApellido());
            response.setDni(dueno.getDni());
            response.setDireccionDomicilio(dueno.getDireccionDomicilio());
        } else if (usuario instanceof Guarderia guarderia) {
            response.setNombreEstablecimiento(guarderia.getNombreEstablecimiento());
            response.setNombreResponsable(guarderia.getNombreResponsable());
            response.setDireccionPredio(guarderia.getDireccionPredio());
        }

        return response;
    }
}