package com.monideal.monideal.dto;

import com.monideal.monideal.model.EstadoPedido;
import com.monideal.monideal.model.Pedido;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Datos del pedido que se envían a las pantallas (JSON y Thymeleaf).
 * Nunca se expone la entidad directamente.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponse {

    private Long idPedido;
    private String clienteNombre;
    private String clienteTelefono;
    private String direccion;
    private String barrio;
    private String indicaciones;
    private String producto;
    private Integer cantidad;
    private EstadoPedido estado;
    private String metodoPago;
    private Boolean pagoConfirmado;
    private String pagaCon;
    private Double subtotal;
    private Double costoEnvio;
    private Double total;
    private String subtotalFormateado;
    private String costoEnvioFormateado;
    private String totalFormateado;
    private List<PedidoItemDto> items;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaEntrega;

    public static PedidoResponse desde(Pedido p) {
        List<PedidoItemDto> itemDtos = (p.getItems() != null && !p.getItems().isEmpty())
                ? p.getItems().stream().map(PedidoItemDto::desde).toList()
                : Collections.emptyList();

        return new PedidoResponse(
                p.getIdPedido(),
                p.getClienteNombre(),
                p.getClienteTelefono(),
                p.getDireccion(),
                p.getBarrio(),
                p.getIndicaciones(),
                p.getProducto(),
                p.getCantidad(),
                p.getEstado(),
                p.getMetodoPago(),
                p.getPagoConfirmado() != null ? p.getPagoConfirmado() : true,
                p.getPagaCon(),
                p.getSubtotal(),
                p.getCostoEnvio(),
                p.getTotal(),
                p.getSubtotalFormateado(),
                p.getCostoEnvioFormateado(),
                p.getTotalFormateado(),
                itemDtos,
                p.getFechaCreacion(),
                p.getFechaAsignacion(),
                p.getFechaEntrega()
        );
    }
}
