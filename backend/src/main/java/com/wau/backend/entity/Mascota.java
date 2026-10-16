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

    // Código único público (ej "WAU-482K")
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    private String raza;

    private Integer edad;

    @Enumerated(EnumType.STRING)
    private TamanoMascota tamano;

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