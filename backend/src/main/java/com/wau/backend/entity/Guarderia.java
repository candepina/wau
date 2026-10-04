package com.wau.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "guarderias")
@Getter
@Setter
public class Guarderia extends Usuario {

    private String nombreEstablecimiento;
    private String nombreResponsable;
    private String direccionPredio;
}