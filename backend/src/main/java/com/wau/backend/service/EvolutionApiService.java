package com.wau.backend.service;

import com.wau.backend.config.EvolutionProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Cliente HTTP contra Evolution API (https://github.com/evolution-foundation/evolution-api).
 * Todos los envios salen por la instancia central de la plataforma (evolution.instance),
 * emparejada una unica vez por QR desde Evolution Manager (http://localhost:3000).
 *
 * Payload de envio de texto, segun el SendTextDto de Evolution API v2:
 * {"number": "<codigo de pais + numero, sin +>", "text": "<texto>"}
 */
@Service
public class EvolutionApiService {

    private final EvolutionProperties properties;
    private final RestClient restClient;

    public EvolutionApiService(EvolutionProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("apikey", properties.apikey())
                .build();
    }

    public void enviarTexto(String telefonoDestino, String texto) {
        restClient.post()
                .uri("/message/sendText/{instance}", properties.instance())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "number", normalizarTelefono(telefonoDestino),
                        "text", texto
                ))
                .retrieve()
                .toBodilessEntity();
    }

    // Evolution API espera el numero en formato internacional sin "+" (ej: 5493511234567)
    private String normalizarTelefono(String telefono) {
        if (telefono == null) {
            throw new IllegalArgumentException("El telefono de destino es obligatorio");
        }
        String digitos = telefono.replaceAll("\\D", "");
        if (digitos.length() < 10) {
            throw new IllegalArgumentException("El telefono de destino no es un numero valido: " + telefono);
        }
        return digitos;
    }
}
