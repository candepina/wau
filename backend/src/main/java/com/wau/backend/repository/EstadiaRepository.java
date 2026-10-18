package com.wau.backend.repository;

import com.wau.backend.entity.Estadia;
import com.wau.backend.entity.EstadoEstadia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface EstadiaRepository extends JpaRepository<Estadia, Long> {
    // Listar todas las estadías de una guardería por estado (ej: solo las "ACTIVA")
    List<Estadia> findByGuarderiaIdAndEstado(Long guarderiaId, EstadoEstadia estado);

    // Listar todas las estadías de una guardería
    List<Estadia> findByGuarderiaId(Long guarderiaId);

    // Verificar si una guardería tiene (hoy) una estadía activa o programada de una mascota
    boolean existsByGuarderiaIdAndMascotaIdAndEstadoIn(Long guarderiaId, Long mascotaId, Collection<EstadoEstadia> estados);

    // Verificar si una mascota tiene estadía activa
    boolean existsByMascotaIdAndEstado(Long mascotaId, EstadoEstadia estado);

    // Verificar si alguna mascota de un dueño tiene estadía activa
    boolean existsByMascotaDuenoIdAndEstado(Long duenoId, EstadoEstadia estado);

    // Verificar si una guardería tiene estadías activas
    boolean existsByGuarderiaIdAndEstado(Long guarderiaId, EstadoEstadia estado);

    // Timeline del dueño: estadías por mascota y estado
    java.util.List<Estadia> findByMascotaIdAndEstado(Long mascotaId, EstadoEstadia estado);

    java.util.Optional<Estadia> findFirstByMascotaIdAndEstadoOrderByFechaInicioAsc(Long mascotaId, EstadoEstadia estado);

    // Promoción PROGRAMADA -> ACTIVA: vencidas por fecha de inicio
    List<Estadia> findByEstadoAndFechaInicioLessThanEqual(EstadoEstadia estado, LocalDate fecha);

    List<Estadia> findByGuarderiaIdAndEstadoAndFechaInicioLessThanEqual(Long guarderiaId, EstadoEstadia estado, LocalDate fecha);
}