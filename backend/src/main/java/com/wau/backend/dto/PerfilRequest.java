package com.wau.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerfilRequest {

    // Campos comunes
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[+]?[0-9\\s().-]{8,20}$", message = "Formato de teléfono inválido")
    private String telefono;

    // Password change (opcional)
    private String passwordActual;
    private String passwordNuevo;
    private String passwordNuevoConfirmacion;

    // Campos específicos Dueño (validados en service según rol)
    private String nombre;
    private String apellido;
    private String dni;
    private String direccionDomicilio;

    // Campos específicos Guardería (validados en service según rol)
    private String nombreEstablecimiento;
    private String nombreResponsable;
    private String direccionPredio;
}