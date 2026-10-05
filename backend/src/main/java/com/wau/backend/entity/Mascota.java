package com.wau.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mascotas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String raza;

    private Integer edad;

    private String tamano; // "PEQUENO", "MEDIANO", "GRANDE"

    @Column(length = 1000)
    private String observacionesMedicas;

    @Column(length = 1000)
    private String observacionesConductuales;

    @Column(length = 1000)
    private String observacionesGenerales;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dueno_id", nullable = false)
    private Dueno dueno;
}