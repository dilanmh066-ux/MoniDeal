package com.monideal.monideal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Copia de un pedido entregado.
 * Se crea cuando el domiciliario confirma la entrega.
 */
@Entity
@Table(name = "historial_ventas")
@Getter
@Setter
@NoArgsConstructor
public class HistorialVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @Column(name = "cliente_nombre", nullable = false, length = 120)
    private String clienteNombre;

    @Column(name = "cliente_telefono", nullable = false, length = 20)
    private String clienteTelefono;

    @Column(name = "direccion", nullable = false, length = 200)
    private String direccion;

    @Column(name = "barrio", nullable = false, length = 100)
    private String barrio;

    @Column(name = "producto", nullable = false, length = 500)
    private String producto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "metodo_pago", length = 30)
    private String metodoPago;

    @Column(name = "paga_con", length = 50)
    private String pagaCon;

    @Column(name = "subtotal")
    private Double subtotal = 0.0;

    @Column(name = "costo_envio")
    private Double costoEnvio = 4000.0;

    @Column(name = "total")
    private Double total = 0.0;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDateTime fechaPedido;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDateTime fechaEntrega;

    public Double getSubtotal() {
        if (subtotal != null && subtotal > 0) return subtotal;
        if (total != null && total > getCostoEnvio()) return total - getCostoEnvio();
        return 64000.0;
    }

    public Double getCostoEnvio() {
        if (costoEnvio != null && costoEnvio > 0) return costoEnvio;
        return 4000.0;
    }

    public Double getTotal() {
        if (total != null && total > 0) return total;
        return getSubtotal() + getCostoEnvio();
    }

    public String getSubtotalFormateado() {
        return formatDinero(getSubtotal());
    }

    public String getCostoEnvioFormateado() {
        return formatDinero(getCostoEnvio());
    }

    public String getTotalFormateado() {
        return formatDinero(getTotal());
    }

    private static String formatDinero(Double valor) {
        if (valor == null || valor <= 0) return "$68.000";
        return String.format("$%,.0f", valor);
    }

    /** Crea la copia del historial a partir de un pedido ya entregado. */
    public static HistorialVenta desde(Pedido p) {
        HistorialVenta h = new HistorialVenta();
        h.setPedido(p);
        h.setClienteNombre(p.getClienteNombre());
        h.setClienteTelefono(p.getClienteTelefono());
        h.setDireccion(p.getDireccion());
        h.setBarrio(p.getBarrio());
        h.setProducto(p.getProducto());
        h.setCantidad(p.getCantidad());
        h.setMetodoPago(p.getMetodoPago());
        h.setPagaCon(p.getPagaCon());
        h.setSubtotal(p.getSubtotal());
        h.setCostoEnvio(p.getCostoEnvio());
        h.setTotal(p.getTotal());
        h.setFechaPedido(p.getFechaCreacion());
        h.setFechaEntrega(p.getFechaEntrega());
        return h;
    }
}
