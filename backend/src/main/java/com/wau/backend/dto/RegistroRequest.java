package com.wau.backend.dto;

import com.wau.backend.entity.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    private String telefono;

    @NotNull(message = "El rol es obligatorio")
    private RolUsuario rol; // ROLE_DUENO o ROLE_GUARDERIA

    // Campos específicos para Dueño
    private String nombre;
    private String apellido;
    private String dni;
    private String direccionDomicilio;

    // Campos específicos para Guardería
    private String nombreEstablecimiento;
    private String nombreResponsable;
    private String direccionPredio;
}