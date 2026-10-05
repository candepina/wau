package com.wau.backend.controller;

import com.wau.backend.dto.MascotaRequest;
import com.wau.backend.dto.MascotaResponse;
import com.wau.backend.service.MascotaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;

    public MascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    @PostMapping
    public ResponseEntity<MascotaResponse> registrarMascota(
            @Valid @RequestBody MascotaRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        MascotaResponse response = mascotaService.registrarMascota(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MascotaResponse>> listarMisMascotas(Authentication authentication) {
        String email = authentication.getName();
        List<MascotaResponse> mascotas = mascotaService.listarMascotasDelDueno(email);
        return ResponseEntity.ok(mascotas);
    }
}