package com.wau.backend.service;

import com.wau.backend.dto.LoginRequest;
import com.wau.backend.dto.LoginResponse;
import com.wau.backend.dto.RegistroRequest;
import com.wau.backend.entity.Dueno;
import com.wau.backend.entity.Guarderia;
import com.wau.backend.entity.RolUsuario;
import com.wau.backend.entity.Usuario;
import com.wau.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse autenticar(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas: email no registrado"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas: contraseña incorrecta");
        }

        return new LoginResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRol(),
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
            dueno.setNombre(request.getNombre());
            dueno.setApellido(request.getApellido());
            dueno.setDni(request.getDni());
            dueno.setDireccionDomicilio(request.getDireccionDomicilio());
            nuevoUsuario = dueno;
        } else if (request.getRol() == RolUsuario.ROLE_GUARDERIA) {
            Guarderia guarderia = new Guarderia();
            guarderia.setNombreEstablecimiento(request.getNombreEstablecimiento());
            guarderia.setNombreResponsable(request.getNombreResponsable());
            guarderia.setDireccionPredio(request.getDireccionPredio());
            nuevoUsuario = guarderia;
        } else {
            throw new RuntimeException("Rol de usuario inválido");
        }

        // Seteo de datos comunes heredados de la clase abstracta Usuario
        nuevoUsuario.setEmail(emailNormalizado);
        nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevoUsuario.setTelefono(request.getTelefono());
        nuevoUsuario.setRol(request.getRol());

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        return new LoginResponse(
                usuarioGuardado.getId(),
                usuarioGuardado.getEmail(),
                usuarioGuardado.getRol(),
                "Usuario registrado exitosamente"
        );
    }

}
