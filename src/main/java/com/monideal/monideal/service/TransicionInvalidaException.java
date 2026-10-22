package com.monideal.monideal.service;

/** Se lanza cuando se pide un cambio de estado que no está permitido (HTTP 409). */
public class TransicionInvalidaException extends RuntimeException {
    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
