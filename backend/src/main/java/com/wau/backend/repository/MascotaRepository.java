package com.wau.backend.repository;

import com.wau.backend.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    List<Mascota> findByDuenoId(Long duenoId);

    // Búsqueda por código de vinculación
    Optional<Mascota> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}