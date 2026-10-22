package com.monideal.monideal.service;

import com.monideal.monideal.dto.PedidoRequest;
import com.monideal.monideal.dto.PedidoResponse;
import com.monideal.monideal.model.EstadoPedido;
import com.monideal.monideal.model.HistorialVenta;
import com.monideal.monideal.model.Pedido;
import com.monideal.monideal.repository.HistorialVentaRepository;
import com.monideal.monideal.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reglas de negocio de los pedidos.
 * Aquí se controla qué cambios de estado están permitidos.
 */
@Service
public class PedidoService {

    /**
     * Catálogo provisional de concentrados.
     * Más adelante se puede cambiar por una tabla "productos" sin tocar las pantallas.
     */
    public static final List<String> CONCENTRADOS = List.of(
            "Chunky Perros Adultos Pollo - 25 kg",
            "Purina Dog Chow Adultos Medianos y Grandes - 22.7 kg",
            "Ringo Croquetas Adultos - 30 kg",
            "Mirringo Gatos Adultos - 10 kg",
            "Donkat Gatos Adultos - 14 kg",
            "Italcol Ponedoras Postura - 40 kg",
            "Solla Pollo Engorde Iniciación - 40 kg"
    );

    private final PedidoRepository pedidoRepository;
    private final HistorialVentaRepository historialVentaRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         HistorialVentaRepository historialVentaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.historialVentaRepository = historialVentaRepository;
    }

    public List<String> listarConcentrados() {
        return CONCENTRADOS;
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstadoOrderByFechaCreacionAsc(estado)
                .stream()
                .map(PedidoResponse::desde)
                .toList();
    }

    /** Cliente: crea el pedido en estado PENDIENTE. */
    @Transactional
    public PedidoResponse crear(PedidoRequest req) {
        String producto = req.producto().trim();
        if (!CONCENTRADOS.contains(producto)) {
            throw new IllegalArgumentException("El concentrado seleccionado no está disponible.");
        }

        Pedido p = new Pedido();
        p.setClienteNombre(req.clienteNombre().trim());
        p.setClienteTelefono(req.clienteTelefono().trim());
        p.setDireccion(req.direccion().trim());
        p.setBarrio(req.barrio().trim());
        p.setIndicaciones(limpiarOpcional(req.indicaciones()));
        p.setProducto(producto);
        p.setCantidad(req.cantidad());
        p.setEstado(EstadoPedido.PENDIENTE);

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
