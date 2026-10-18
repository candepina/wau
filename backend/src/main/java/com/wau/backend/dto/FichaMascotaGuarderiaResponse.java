package com.wau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ficha completa de una mascota para su guardería (solo lectura).
 * Incluye datos de contacto del dueño para que el cuidador pueda comunicarse.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FichaMascotaGuarderiaResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private String raza;
    private Integer edad;
    private String tamano;
    private String observacionesMedicas;
    private String observacionesConductuales;
    private String observacionesGenerales;
    private String duenoNombre;
    private String duenoTelefono;
}
