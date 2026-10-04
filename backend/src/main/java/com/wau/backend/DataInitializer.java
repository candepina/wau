package com.wau.backend;

import com.wau.backend.entity.Dueno;
import com.wau.backend.entity.RolUsuario;
import com.wau.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String emailPrueba = "dueno@wau.com";
        if (!usuarioRepository.existsByEmail(emailPrueba)) {
            Dueno dueno = new Dueno();
            dueno.setEmail(emailPrueba);
            dueno.setPassword(passwordEncoder.encode("123456"));
            dueno.setTelefono("+5493511234567");
            dueno.setRol(RolUsuario.ROLE_DUENO);
            dueno.setNombre("Juan");
            dueno.setApellido("Pérez");
            dueno.setDni("35123456");
            dueno.setDireccionDomicilio("Calle Falsa 123, Córdoba");

            usuarioRepository.save(dueno);
            System.out.println(">>> Usuario de prueba 'dueno@wau.com' creado con clave encriptada.");
        }
    }
}