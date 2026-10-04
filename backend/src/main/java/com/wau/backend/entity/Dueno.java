package com.wau.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "duenos")
@Getter
@Setter
public class Dueno extends Usuario {

    private String nombre;
    private String apellido;
    private String dni;
    private String direccionDomicilio;
}