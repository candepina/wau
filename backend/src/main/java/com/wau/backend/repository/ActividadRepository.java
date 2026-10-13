package com.wau.backend.repository;

import com.wau.backend.entity.Actividad;
import com.wau.backend.entity.TipoActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    List<Actividad> findByEstadiaIdOrderByFechaHoraDesc(Long estadiaId);
    boolean existsByEstadiaIdAndTipo(Long estadiaId, TipoActividad tipo);
}