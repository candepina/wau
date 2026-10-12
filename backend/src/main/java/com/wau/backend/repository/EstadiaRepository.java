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

    // Verificar si una mascota tiene estadía activa
    boolean existsByMascotaIdAndEstado(Long mascotaId, String estado);

    // Verificar si alguna mascota de un dueño tiene estadía activa
    boolean existsByMascotaDuenoIdAndEstado(Long duenoId, String estado);

    // Verificar si una guardería tiene estadías activas
    boolean existsByGuarderiaIdAndEstado(Long guarderiaId, String estado);
}