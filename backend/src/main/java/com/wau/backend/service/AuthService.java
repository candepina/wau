package com.wau.backend.service;

import com.wau.backend.dto.LoginRequest;
import com.wau.backend.dto.LoginResponse;
import com.wau.backend.dto.RegistroRequest;
import com.wau.backend.entity.Dueno;
import com.wau.backend.entity.Guarderia;
import com.wau.backend.entity.RolUsuario;
import com.wau.backend.entity.Usuario;
import com.wau.backend.repository.UsuarioRepository;
import com.wau.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse autenticar(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas: email no registrado"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas: contraseña incorrecta");
        }

        String token = jwtService.generarToken(usuario.getEmail(), usuario.getRol().name(), usuario.getId());

        return new LoginResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRol(),
                token,
                "Autenticación exitosa"
        );
    }

    public LoginResponse registrar(RegistroRequest request) {
        String emailNormalizado = request.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new RuntimeException("El correo electrónico ya se encuentra registrado");
        }

        Usuario nuevoUsuario;

        if (request.getRol() == RolUsuario.ROLE_DUENO) {
            Dueno dueno = new Dueno();
            dueno.setNombre(obligatorio(request.getNombre(), "El nombre es obligatorio"));
            dueno.setApellido(obligatorio(request.getApellido(), "El apellido es obligatorio"));

            String dni = obligatorio(request.getDni(), "El DNI es obligatorio");
            if (!dni.matches("\\d{7,8}")) {
                throw new RuntimeException("El DNI debe tener 7 u 8 dígitos numéricos");
            }
            dueno.setDni(dni);

            dueno.setDireccionDomicilio(obligatorio(request.getDireccionDomicilio(), "La dirección de domicilio es obligatoria"));
            nuevoUsuario = dueno;
        } else if (request.getRol() == RolUsuario.ROLE_GUARDERIA) {
            Guarderia guarderia = new Guarderia();
            guarderia.setNombreEstablecimiento(obligatorio(request.getNombreEstablecimiento(), "El nombre del establecimiento es obligatorio"));
            guarderia.setNombreResponsable(obligatorio(request.getNombreResponsable(), "El nombre del responsable es obligatorio"));
            guarderia.setDireccionPredio(obligatorio(request.getDireccionPredio(), "La dirección del predio es obligatoria"));
            nuevoUsuario = guarderia;
        } else {
            throw new RuntimeException("Rol de usuario inválido");
        }

        nuevoUsuario.setEmail(emailNormalizado);
        nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevoUsuario.setTelefono(request.getTelefono());
        nuevoUsuario.setRol(request.getRol());

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        String token = jwtService.generarToken(usuarioGuardado.getEmail(), usuarioGuardado.getRol().name(), usuarioGuardado.getId());

        return new LoginResponse(
                usuarioGuardado.getId(),
                usuarioGuardado.getEmail(),
                usuarioGuardado.getRol(),
                token,
                "Usuario registrado exitosamente"
        );
    }

    // Regla: los campos del perfil son obligatorios según el rol que se registra.
    // Mismo criterio que UsuarioService.actualizarPerfil (validarCampoObligatorio).
    private String obligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new RuntimeException(mensaje);
        }
        return valor.trim();
    }
}