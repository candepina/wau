package com.wau.backend.service;

import com.wau.backend.config.EvolutionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Notificaciones automatizadas de la plataforma a los dueños por WhatsApp.
 * El número desde el que se envían es el de Wau (instancia central de Evolution),
 * no el de cada guardería. El destino es el teléfono persistido en la cuenta del dueño.
 *
 * El envío es "best effort": cualquier falla (Evolution caído, instancia desconectada,
 * teléfono inválido) se loggea y nunca interrumpe la operación del API.
 */
@Slf4j
@Service
public class NotificacionService {

    private static final String MENSAJE_NUEVA_ACTIVIDAD =
            "🐾 *¡Hola! Ha habido una nueva actualización en el timeline de tu mascota.*\n\n"
            + "Ingresá a la app de Wau para revisar las novedades.";

    private final EvolutionApiService evolutionApiService;
    private final EvolutionProperties evolutionProperties;

    public NotificacionService(EvolutionApiService evolutionApiService, EvolutionProperties evolutionProperties) {
        this.evolutionApiService = evolutionApiService;
        this.evolutionProperties = evolutionProperties;
    }

    /**
     * Notifica al dueño que hay novedades en el timeline de una estadía activa.
     * Se ejecuta asincrónicamente para no demorar la respuesta del endpoint del cuidador.
     */
    @Async
    public void notificarNuevaActividad(String telefonoDueno, Long estadiaId) {
        if (!evolutionProperties.enabled()) {
            return;
        }
        if (telefonoDueno == null || telefonoDueno.isBlank()) {
            log.warn("No se notifica la estadía {}: el dueño no tiene teléfono registrado", estadiaId);
            return;
        }
        try {
            evolutionApiService.enviarTexto(telefonoDueno, MENSAJE_NUEVA_ACTIVIDAD);
            log.info("Notificación WhatsApp enviada al dueño de la estadía {}", estadiaId);
        } catch (Exception e) {
            log.warn("No se pudo enviar la notificación WhatsApp de la estadía {}: {}", estadiaId, e.getMessage());
        }
    }
}
