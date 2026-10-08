package com.wau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadiaResponse {
    private Long id;
    private Long mascotaId;
    private String nombreMascota;
    private String razaMascota;
    private String nombreDueno;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private String notasIngreso;
}