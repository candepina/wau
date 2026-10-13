package com.wau.backend.dto;

import com.wau.backend.entity.TipoActividad;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActividadResponse {

    private Long id;
    private TipoActividad tipo;
    private LocalDateTime fechaHora;
}