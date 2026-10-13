package com.wau.backend.dto;

import com.wau.backend.entity.TipoActividad;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActividadRequest {

    @NotNull(message = "El tipo de actividad es obligatorio")
    private TipoActividad tipo;
}