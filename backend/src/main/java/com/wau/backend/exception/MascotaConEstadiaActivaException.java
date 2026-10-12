package com.wau.backend.exception;

public class MascotaConEstadiaActivaException extends RuntimeException {
    public MascotaConEstadiaActivaException() {
        super("No se puede eliminar la mascota porque tiene una estadía activa en una guardería.");
    }
}