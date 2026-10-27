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

    // Campos comunes (obligatorios en ambos roles) — validados por Bean Validation
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

    // Campos del perfil según el rol: se validan en AuthService.registrar()
    // (mismo criterio que PerfilRequest en UsuarioService)
    private String nombre;
    private String apellido;
    private String dni;
    private String direccionDomicilio;
    private String nombreEstablecimiento;
    private String nombreResponsable;
    private String direccionPredio;
}
