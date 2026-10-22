package com.monideal.monideal.service;

/** Se lanza cuando no se encuentra el pedido (HTTP 404). */
public class PedidoNoEncontradoException extends RuntimeException {
    public PedidoNoEncontradoException(Long id) {
        super("El pedido #" + id + " no existe.");
    }
}
