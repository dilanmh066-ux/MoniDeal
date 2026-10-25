package com.monideal.monideal.dto;

import com.monideal.monideal.model.HistorialVenta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HistorialVentaResponse {
    private Long idHistorial;
    private Long idPedido;
    private String clienteNombre;
    private String clienteTelefono;
    private String direccion;
    private String barrio;
    private String producto;
    private Integer cantidad;
    private String metodoPago;
    private String pagaCon;
    private Double subtotal;
    private Double costoEnvio;
    private Double total;
    private String totalFormateado;
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaEntrega;

    public static HistorialVentaResponse desde(HistorialVenta h) {
        return new HistorialVentaResponse(
                h.getIdHistorial(),
                h.getPedido() != null ? h.getPedido().getIdPedido() : null,
                h.getClienteNombre(),
                h.getClienteTelefono(),
                h.getDireccion(),
                h.getBarrio(),
                h.getProducto(),
                h.getCantidad(),
                h.getMetodoPago(),
                h.getPagaCon(),
                h.getSubtotal(),
                h.getCostoEnvio(),
                h.getTotal(),
                h.getTotalFormateado(),
                h.getFechaPedido(),
                h.getFechaEntrega()
        );
    }
}
