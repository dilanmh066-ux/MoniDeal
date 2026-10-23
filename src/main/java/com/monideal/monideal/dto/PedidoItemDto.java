package com.monideal.monideal.dto;

import com.monideal.monideal.model.PedidoItem;

/**
 * Representa un ítem en las peticiones y respuestas de la API.
 */
public record PedidoItemDto(
        String producto,
        String tipoUnidad, // "BULTO", "LIBRA", "KILO", "VALOR"
        Double cantidad,
        Double valorDinero,
        String detalle
) {
    public static PedidoItemDto desde(PedidoItem item) {
        return new PedidoItemDto(
                item.getProducto(),
                item.getTipoUnidad(),
                item.getCantidad(),
                item.getValorDinero(),
                item.getDetalle()
        );
    }
}
