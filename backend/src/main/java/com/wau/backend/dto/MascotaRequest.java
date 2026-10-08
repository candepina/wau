package com.wau.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MascotaRequest {

    @NotBlank(message = "El nombre de la mascota es obligatorio")
    private String nombre;
    private String raza;
    private Integer edad;
    private String tamano;
    private String observacionesMedicas;
    private String observacionesConductuales;
    private String observacionesGenerales;
}