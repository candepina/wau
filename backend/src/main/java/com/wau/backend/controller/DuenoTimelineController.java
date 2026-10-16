package com.wau.backend.controller;

import com.wau.backend.dto.ActividadResponse;
import com.wau.backend.dto.EstadiaResponse;
import com.wau.backend.service.EstadiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dueno")
public class DuenoTimelineController {

    private final EstadiaService estadiaService;

    public DuenoTimelineController(EstadiaService estadiaService) {
        this.estadiaService = estadiaService;
    }

    @GetMapping("/mascotas/{mascotaId}/estadia-actual")
    public ResponseEntity<EstadiaResponse> obtenerEstadiaActual(
            @PathVariable Long mascotaId,
            Authentication authentication) {
        return ResponseEntity.ok(
                estadiaService.obtenerEstadiaActualDeMiMascota(mascotaId, authentication.getName()));
    }

    @GetMapping("/estadias/{estadiaId}/actividades")
    public ResponseEntity<List<ActividadResponse>> listarActividades(
            @PathVariable Long estadiaId,
            Authentication authentication) {
        return ResponseEntity.ok(
                estadiaService.listarActividadesParaDueno(estadiaId, authentication.getName()));
    }
}
