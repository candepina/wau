package com.wau.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "guarderias")
@Getter
@Setter
public class Guarderia extends Usuario {

    @Column(nullable = false)
    private String nombreEstablecimiento;

    @Column(nullable = false)
    private String nombreResponsable;

    @Column(nullable = false)
    private String direccionPredio;
}