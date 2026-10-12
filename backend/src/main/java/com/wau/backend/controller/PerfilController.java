package com.wau.backend.controller;

import com.wau.backend.dto.PerfilRequest;
import com.wau.backend.dto.PerfilResponse;
import com.wau.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<PerfilResponse> obtenerPerfil(Authentication authentication) {
        String email = authentication.getName();
        PerfilResponse response = usuarioService.obtenerPerfil(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<PerfilResponse> actualizarPerfil(
            @Valid @RequestBody PerfilRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        PerfilResponse response = usuarioService.actualizarPerfil(email, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> eliminarCuenta(Authentication authentication) {
        String email = authentication.getName();
        usuarioService.eliminarCuenta(email);
        return ResponseEntity.noContent().build();
    }
}