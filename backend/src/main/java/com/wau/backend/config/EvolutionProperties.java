package com.wau.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracion de Evolution API (WhatsApp), levantada desde application.properties.
 * La apikey debe coincidir con AUTHENTICATION_API_KEY del contenedor evolution-api.
 */
@ConfigurationProperties("evolution")
public record EvolutionProperties(
        boolean enabled,
        String baseUrl,
        String instance,
        String apikey
) {}
