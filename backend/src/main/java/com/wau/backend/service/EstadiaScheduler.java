package com.wau.backend.service;

import com.wau.backend.entity.Estadia;
import com.wau.backend.entity.EstadoEstadia;
import com.wau.backend.repository.EstadiaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class EstadiaScheduler {

    private final EstadiaRepository estadiaRepository;

    public EstadiaScheduler(EstadiaRepository estadiaRepository) {
        this.estadiaRepository = estadiaRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void activarProgramadasVencidas() {
        List<Estadia> vencidas = estadiaRepository
                .findByEstadoAndFechaInicioLessThanEqual(EstadoEstadia.PROGRAMADA, LocalDate.now());
        for (Estadia e : vencidas) {
            e.activar();
        }
        if (!vencidas.isEmpty()) {
            estadiaRepository.saveAll(vencidas);
        }
    }
}
