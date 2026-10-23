package com.monideal.monideal.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa un producto individual dentro de un pedido (carrito multi-producto).
 * Soporta bultos cerrados, libras/kilos a granel o pedidos por valor en dinero.
 */
@Entity
@Table(name = "pedido_items")
@Getter
@Setter
@NoArgsConstructor
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Long idItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    @JsonBackReference
    private Pedido pedido;

    @Column(name = "producto", nullable = false, length = 150)
    private String producto;

    /** BULTO, LIBRA, KILO, VALOR */
    @Column(name = "tipo_unidad", nullable = false, length = 30)
    private String tipoUnidad = "BULTO";

    @Column(name = "cantidad")
    private Double cantidad = 1.0;

    @Column(name = "valor_dinero")
    private Double valorDinero;

    @Column(name = "detalle", nullable = false, length = 120)
    private String detalle;

    public PedidoItem(String producto, String tipoUnidad, Double cantidad, Double valorDinero, String detalle) {
        this.producto = producto;
        this.tipoUnidad = tipoUnidad != null ? tipoUnidad : "BULTO";
        this.cantidad = cantidad != null ? cantidad : 1.0;
        this.valorDinero = valorDinero;
        this.detalle = detalle != null ? detalle : producto;
    }
}
