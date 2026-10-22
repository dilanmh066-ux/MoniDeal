package com.monideal.monideal.dto;

import jakarta.validation.constraints.*;

/** JSON que envía el cliente al confirmar su pedido. */
public record PedidoRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre es muy largo")
        String clienteNombre,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^[0-9]{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos")
        String clienteTelefono,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200, message = "La dirección es muy larga")
        String direccion,

        @NotBlank(message = "El barrio es obligatorio")
        @Size(max = 100, message = "El barrio es muy largo")
        String barrio,

        @Size(max = 300, message = "Las indicaciones son muy largas")
        String indicaciones,

        @NotBlank(message = "Debe seleccionar un concentrado")
        String producto,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 50, message = "La cantidad máxima es 50")
        Integer cantidad
) {
}
