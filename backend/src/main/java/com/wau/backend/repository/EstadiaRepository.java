package com.wau.backend.repository;

import com.wau.backend.entity.Estadia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstadiaRepository extends JpaRepository<Estadia, Long> {
    // Listar todas las estadías de una guardería por estado (ej: solo las "ACTIVA")
    List<Estadia> findByGuarderiaIdAndEstado(Long guarderiaId, String estado);

    // Listar todas las estadías de una guardería
    List<Estadia> findByGuarderiaId(Long guarderiaId);
}