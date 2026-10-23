package com.monideal.monideal.service;

import com.monideal.monideal.dto.PedidoItemDto;
import com.monideal.monideal.dto.PedidoRequest;
import com.monideal.monideal.dto.PedidoResponse;
import com.monideal.monideal.model.EstadoPedido;
import com.monideal.monideal.model.HistorialVenta;
import com.monideal.monideal.model.Pedido;
import com.monideal.monideal.model.PedidoItem;
import com.monideal.monideal.repository.HistorialVentaRepository;
import com.monideal.monideal.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reglas de negocio de los pedidos y el catálogo.
 * Soporta bultos cerrados, venta a granel (libras/kilos/dinero) y confirmación de pagos.
 */
@Service
public class PedidoService {

    /**
     * Catálogo de concentrados en bulto cerrado.
     */
    public static final List<String> CONCENTRADOS_BULTOS = List.of(
            "Chunky Perros Adultos Pollo - 25 kg",
            "Purina Dog Chow Adultos Medianos y Grandes - 22.7 kg",
            "Ringo Croquetas Adultos - 30 kg",
            "Mirringo Gatos Adultos - 10 kg",
            "Donkat Gatos Adultos - 14 kg",
            "Italcol Ponedoras Postura - 40 kg",
            "Solla Pollo Engorde Iniciación - 40 kg",
            "Monello Perros Adultos - 15 kg",
            "Cat Chow Gatitos Pollo - 15 kg"
    );

    /**
     * Catálogo de productos disponibles a granel (por libras, kilos o monto de dinero).
     */
    public static final List<String> PRODUCTOS_GRANEL = List.of(
            "Alpiste Limpio",
            "Maíz Amarillo Entero",
            "Maíz Amarillo Partido",
            "Mezcla Especial para Canarios",
            "Q-ida Can Perro Adulto",
            "Mogolla de Trigo",
            "Sal Mineralizada"
    );

    private final PedidoRepository pedidoRepository;
    private final HistorialVentaRepository historialVentaRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         HistorialVentaRepository historialVentaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.historialVentaRepository = historialVentaRepository;
    }

    public List<String> listarConcentrados() {
        return CONCENTRADOS_BULTOS;
    }

    public List<String> listarGranel() {
        return PRODUCTOS_GRANEL;
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstadoOrderByFechaCreacionAsc(estado)
                .stream()
                .map(PedidoResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistorialVenta> listarHistorial() {
        return historialVentaRepository.findAllByOrderByFechaEntregaDesc();
    }

    /**
     * Cliente: crea el pedido en estado PENDIENTE.
     * Procesa múltiples productos o el formato individual legado.
     */
    @Transactional
    public PedidoResponse crear(PedidoRequest req) {
        Pedido p = new Pedido();
        p.setClienteNombre(req.clienteNombre().trim());
        p.setClienteTelefono(req.clienteTelefono().trim());
        p.setDireccion(req.direccion().trim());
        p.setBarrio(req.barrio().trim());
        p.setIndicaciones(limpiarOpcional(req.indicaciones()));

        // Método de pago
        String metodo = (req.metodoPago() != null && !req.metodoPago().isBlank())
                ? req.metodoPago().trim().toUpperCase()
                : "EFECTIVO";
        p.setMetodoPago(metodo);
        p.setPagoConfirmado("EFECTIVO".equals(metodo)); // Efectivo nace confirmado; transferencia espera revisión
        p.setPagaCon(limpiarOpcional(req.pagaCon()));

        // Procesar ítems (carrito multi-producto)
        List<PedidoItem> itemsAGuardar = new ArrayList<>();
        if (req.items() != null && !req.items().isEmpty()) {
            for (PedidoItemDto itemDto : req.items()) {
                if (itemDto.producto() == null || itemDto.producto().isBlank()) continue;

                String prod = itemDto.producto().trim();
                String tipo = (itemDto.tipoUnidad() != null && !itemDto.tipoUnidad().isBlank())
                        ? itemDto.tipoUnidad().trim().toUpperCase() : "BULTO";
                Double cant = itemDto.cantidad() != null ? itemDto.cantidad() : 1.0;
                Double valor = itemDto.valorDinero();

                String detalle = itemDto.detalle();
                if (detalle == null || detalle.isBlank()) {
                    if ("VALOR".equals(tipo) && valor != null) {
                        detalle = String.format("$%,.0f de %s", valor, prod);
                    } else if ("LIBRA".equals(tipo)) {
                        detalle = String.format("%.0f lb de %s", cant, prod);
                    } else if ("KILO".equals(tipo)) {
                        detalle = String.format("%.0f kg de %s", cant, prod);
                    } else {
                        detalle = String.format("%.0f bulto(s) de %s", cant, prod);
                    }
                }

                PedidoItem item = new PedidoItem(prod, tipo, cant, valor, detalle);
                itemsAGuardar.add(item);
                p.agregarItem(item);
            }
        }

        // Si no mandó lista de items pero mandó producto único (soporte formularios legados)
        if (itemsAGuardar.isEmpty()) {
            String prodUnico = req.producto() != null ? req.producto().trim() : "Concentrado";
            int cantUnica = (req.cantidad() != null && req.cantidad() > 0) ? req.cantidad() : 1;
            PedidoItem item = new PedidoItem(prodUnico, "BULTO", (double) cantUnica, null, cantUnica + " bulto(s) de " + prodUnico);
            itemsAGuardar.add(item);
            p.agregarItem(item);
        }

        // Armar resumen legible para la columna producto y cantidad
        String resumenTexto = itemsAGuardar.stream()
                .map(PedidoItem::getDetalle)
                .collect(Collectors.joining(" + "));
        if (resumenTexto.length() > 490) {
            resumenTexto = resumenTexto.substring(0, 485) + "...";
        }
        p.setProducto(resumenTexto);
        p.setCantidad(Math.max(1, itemsAGuardar.size()));
        p.setEstado(EstadoPedido.PENDIENTE);

        return PedidoResponse.desde(pedidoRepository.save(p));
    }

    /**
     * Oficina o Cajera: Confirma la recepción de la transferencia bancaria.
     */
    @Transactional
    public PedidoResponse confirmarPago(Long id) {
        Pedido p = buscar(id);
        p.setPagoConfirmado(true);
        return PedidoResponse.desde(pedidoRepository.save(p));
    }

    /** Cajera: PENDIENTE -> EN_RUTA. */
    @Transactional
    public PedidoResponse asignarADomicilio(Long id) {
        Pedido p = buscar(id);
        validarEstado(p, EstadoPedido.PENDIENTE, "Solo se pueden asignar pedidos PENDIENTES.");

        p.setEstado(EstadoPedido.EN_RUTA);
        p.setFechaAsignacion(LocalDateTime.now());
        return PedidoResponse.desde(pedidoRepository.save(p));
    }

    /** Domiciliario: EN_RUTA -> ENTREGADO y copia al historial de ventas. */
    @Transactional
    public PedidoResponse confirmarEntrega(Long id) {
        Pedido p = buscar(id);
        validarEstado(p, EstadoPedido.EN_RUTA, "Solo se pueden entregar pedidos EN RUTA.");

        p.setEstado(EstadoPedido.ENTREGADO);
        p.setFechaEntrega(LocalDateTime.now());
        Pedido guardado = pedidoRepository.save(p);

        if (!historialVentaRepository.existsByPedidoIdPedido(guardado.getIdPedido())) {
            historialVentaRepository.save(HistorialVenta.desde(guardado));
        }
        return PedidoResponse.desde(guardado);
    }

    // ---------- Métodos de apoyo ----------

    private Pedido buscar(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNoEncontradoException(id));
    }

    private void validarEstado(Pedido p, EstadoPedido esperado, String mensaje) {
        if (p.getEstado() != esperado) {
            throw new TransicionInvalidaException(
                    mensaje + " Estado actual: " + p.getEstado() + ".");
        }
    }

    private String limpiarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
