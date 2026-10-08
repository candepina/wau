package com.wau.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "estadias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Estadia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @Column(nullable = false)
    private String estado; // "ACTIVA", "FINALIZADA"

    @Column(length = 1000)
    private String notasIngreso; // Pertenencias con las que ingresa, indicaciones puntuales

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guarderia_id", nullable = false)
    private Guarderia guarderia;
}