package com.wau.backend.exception;

public class MascotaNotFoundException extends RuntimeException {
    public MascotaNotFoundException(Long id) {
        super("Mascota no encontrada con ID: " + id);
    }
}