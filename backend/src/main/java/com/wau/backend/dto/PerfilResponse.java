package com.wau.backend.dto;

import com.wau.backend.entity.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilResponse {

    private Long id;
    private String email;
    private String telefono;
    private RolUsuario rol;
    private LocalDateTime fechaAlta;

    // Campos específicos Dueño
    private String nombre;
    private String apellido;
    private String dni;
    private String direccionDomicilio;

    // Campos específicos Guardería
    private String nombreEstablecimiento;
    private String nombreResponsable;
    private String direccionPredio;
}