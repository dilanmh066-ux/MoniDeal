package com.monideal.monideal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedido;

    // ---------- Datos de entrega del cliente ----------

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120)
    @Column(name = "cliente_nombre", nullable = false, length = 120)
    private String clienteNombre;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos")
    @Column(name = "cliente_telefono", nullable = false, length = 20)
    private String clienteTelefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 200)
    @Column(name = "direccion", nullable = false, length = 200)
    private String direccion;

    @NotBlank(message = "El barrio es obligatorio")
    @Size(max = 100)
    @Column(name = "barrio", nullable = false, length = 100)
    private String barrio;

    @Size(max = 300)
    @Column(name = "indicaciones", length = 300)
    private String indicaciones;

    // ---------- Producto y Carrito ----------

    @NotBlank(message = "Debe haber al menos un producto en el pedido")
    @Size(max = 500)
    @Column(name = "producto", nullable = false, length = 500)
    private String producto;

    @NotNull
    @Min(1)
    @Max(100)
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 1;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PedidoItem> items = new ArrayList<>();

    // ---------- Pago y trazabilidad ----------

    @Column(name = "metodo_pago", length = 30)
    private String metodoPago = "EFECTIVO"; // EFECTIVO, TRANSFERENCIA

    @Column(name = "pago_confirmado")
    private Boolean pagoConfirmado = true; // true para efectivo, false para transferencia hasta confirmar

    @Column(name = "paga_con", length = 50)
    private String pagaCon; // Ej: "Con billete de $50.000" para cambio

    // ---------- Valores económicos ----------

    @Column(name = "subtotal")
    private Double subtotal = 0.0;

    @Column(name = "costo_envio")
    private Double costoEnvio = 4000.0;

    @Column(name = "total")
    private Double total = 0.0;

    // ---------- Estado y trazabilidad ----------

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false,
            columnDefinition = "ENUM('PENDIENTE','EN_RUTA','ENTREGADO')")
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    public void agregarItem(PedidoItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        item.setPedido(this);
    }

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

    @PrePersist
    void alCrear() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoPedido.PENDIENTE;
        }
        if (metodoPago == null || metodoPago.isBlank()) {
            metodoPago = "EFECTIVO";
        }
        if (pagoConfirmado == null) {
            pagoConfirmado = "EFECTIVO".equalsIgnoreCase(metodoPago);
        }
    }
}
