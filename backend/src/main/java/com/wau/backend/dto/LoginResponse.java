package com.wau.backend.dto;

import com.wau.backend.entity.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String email;
    private RolUsuario rol;
    private String token;
    private String mensaje;

}