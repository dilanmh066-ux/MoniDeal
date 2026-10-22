package com.monideal.monideal.controller;

import com.monideal.monideal.service.PedidoNoEncontradoException;
import com.monideal.monideal.service.TransicionInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convierte los errores en JSON claros para el frontend:
 * { "mensaje": "...", "errores": { "campo": "detalle" } }
 */
@RestControllerAdvice(assignableTypes = PedidoController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return respuesta(HttpStatus.BAD_REQUEST, "Revisa los datos del formulario.", errores);
    }

    @ExceptionHandler({IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> peticionInvalida(Exception ex) {
        String mensaje = ex instanceof IllegalArgumentException
                ? ex.getMessage()
                : "La petición no tiene un formato válido.";
        return respuesta(HttpStatus.BAD_REQUEST, mensaje, null);
    }

    @ExceptionHandler(PedidoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(PedidoNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(TransicionInvalidaException.class)
    public ResponseEntity<Map<String, Object>> conflicto(TransicionInvalidaException ex) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus status, String mensaje,
                                                          Map<String, String> errores) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensaje", mensaje);
        if (errores != null && !errores.isEmpty()) {
            body.put("errores", errores);
        }
        return ResponseEntity.status(status).body(body);
    }
}
