package com.wau.backend.dto;

import com.wau.backend.entity.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9\\s]{8,15}$", message = "Formato de teléfono inválido")
    private String telefono;

    @NotNull(message = "El rol es obligatorio")
    private RolUsuario rol; // ROLE_DUENO o ROLE_GUARDERIA

    // Campos específicos para Dueño
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^\\d{7,8}$", message = "El DNI debe tener 7 u 8 dígitos numéricos")
    private String dni;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccionDomicilio;

    // Campos específicos para Guardería
    @NotBlank(message = "El nombre del establecimiento es obligatorio")
    private String nombreEstablecimiento;

    @NotBlank(message = "El nombre del responsable es obligatorio")
    private String nombreResponsable;

    @NotBlank(message = "La dirección del predio es obligatoria")
    private String direccionPredio;
}