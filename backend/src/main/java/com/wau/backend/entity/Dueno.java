package com.wau.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "duenos")
@Getter
@Setter
public class Dueno extends Usuario {

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false)
    private String dni;

    @Column(nullable = false)
    private String direccionDomicilio;

    @OneToMany(mappedBy = "dueno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mascota> mascotas = new ArrayList<>();

    public void agregarMascota(Mascota mascota) {
        mascotas.add(mascota);
        mascota.setDueno(this);
    }

    public void removerMascota(Mascota mascota) {
        mascotas.remove(mascota);
        mascota.setDueno(null);
    }
}