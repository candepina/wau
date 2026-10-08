package com.wau.backend.controller;

import com.wau.backend.dto.EstadiaRequest;
import com.wau.backend.dto.EstadiaResponse;
import com.wau.backend.service.EstadiaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estadias")
public class EstadiaController {

    private final EstadiaService estadiaService;

    public EstadiaController(EstadiaService estadiaService) {
        this.estadiaService = estadiaService;
    }

    @PostMapping
    public ResponseEntity<EstadiaResponse> crearEstadia(
            @Valid @RequestBody EstadiaRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        EstadiaResponse response = estadiaService.crearEstadia(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/activas")
    public ResponseEntity<List<EstadiaResponse>> listarActivas(Authentication authentication) {
        String email = authentication.getName();
        List<EstadiaResponse> activas = estadiaService.listarEstadiasActivas(email);
        return ResponseEntity.ok(activas);
    }
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<EstadiaResponse> finalizarEstadia(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        EstadiaResponse response = estadiaService.finalizarEstadia(email, id);
        return ResponseEntity.ok(response);
    }

}