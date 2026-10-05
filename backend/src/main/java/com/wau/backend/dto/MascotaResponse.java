package com.wau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MascotaResponse {
    private Long id;
    private String nombre;
    private String raza;
    private Integer edad;
    private String tamano;
    private String observacionesMedicas;
    private String observacionesConductuales;
    private String observacionesGenerales;
    private Long duenoId;
}