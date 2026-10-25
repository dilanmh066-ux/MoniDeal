package com.monideal.monideal.service;

import com.monideal.monideal.dto.PedidoItemDto;
import com.monideal.monideal.dto.PedidoRequest;
import com.monideal.monideal.dto.PedidoResponse;
import com.monideal.monideal.dto.ProductoCatalogoDto;
import com.monideal.monideal.dto.ResumenCierreDto;
import com.monideal.monideal.model.EstadoPedido;
import com.monideal.monideal.model.HistorialVenta;
import com.monideal.monideal.model.Pedido;
import com.monideal.monideal.model.PedidoItem;
import com.monideal.monideal.repository.HistorialVentaRepository;
import com.monideal.monideal.repository.PedidoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reglas de negocio de los pedidos, precios y catálogo de Guadalajara de Buga.
 * Soporta cálculo comercial de subtotales, costo de domicilio, totales a cobrar
 * y métricas de cierre diario de caja.
 */
@Service
public class PedidoService {

    public static final Double COSTO_DOMICILIO_BUGA = 4000.0;

    /**
     * Catálogo enriquecido de concentrados en bulto cerrado con precios en COP.
     */
    public static final List<ProductoCatalogoDto> CATALOGO_BULTOS = List.of(
            new ProductoCatalogoDto("Chunky Perros Adultos Pollo - 25 kg", 148000.0, "$148.000", "BULTO", "25 kg", "Perros", "Más vendido"),
            new ProductoCatalogoDto("Purina Dog Chow Adultos Medianos y Grandes - 22.7 kg", 156000.0, "$156.000", "BULTO", "22.7 kg", "Perros", "Gama Pro"),
            new ProductoCatalogoDto("Ringo Croquetas Adultos - 30 kg", 128000.0, "$128.000", "BULTO", "30 kg", "Perros", "Económico"),
            new ProductoCatalogoDto("Mirringo Gatos Adultos - 10 kg", 69000.0, "$69.000", "BULTO", "10 kg", "Gatos", "Top Gatos"),
            new ProductoCatalogoDto("Donkat Gatos Adultos - 14 kg", 89000.0, "$89.000", "BULTO", "14 kg", "Gatos", "Ahorro"),
            new ProductoCatalogoDto("Italcol Ponedoras Postura - 40 kg", 98000.0, "$98.000", "BULTO", "40 kg", "Aves", "Granja"),
            new ProductoCatalogoDto("Solla Pollo Engorde Iniciación - 40 kg", 106000.0, "$106.000", "BULTO", "40 kg", "Aves", "Engorde"),
            new ProductoCatalogoDto("Monello Perros Adultos - 15 kg", 119000.0, "$119.000", "BULTO", "15 kg", "Perros", "Premium"),
            new ProductoCatalogoDto("Cat Chow Gatitos Pollo - 15 kg", 134000.0, "$134.000", "BULTO", "15 kg", "Gatos", "Especial")
    );

    /**
     * Catálogo enriquecido de productos a granel (precio por libra en COP).
     */
    public static final List<ProductoCatalogoDto> CATALOGO_GRANEL = List.of(
            new ProductoCatalogoDto("Alpiste Limpio", 3500.0, "$3.500 / lb", "GRANEL", "Libra", "Aves", "Limpio"),
            new ProductoCatalogoDto("Maíz Amarillo Entero", 2200.0, "$2.200 / lb", "GRANEL", "Libra", "Granos", "Seleccionado"),
            new ProductoCatalogoDto("Maíz Amarillo Partido", 2400.0, "$2.400 / lb", "GRANEL", "Libra", "Granos", "Calidad"),
            new ProductoCatalogoDto("Mezcla Especial para Canarios", 4200.0, "$4.200 / lb", "GRANEL", "Libra", "Aves", "Nutritivo"),
            new ProductoCatalogoDto("Q-ida Can Perro Adulto", 3800.0, "$3.800 / lb", "GRANEL", "Libra", "Perros", "Porción"),
            new ProductoCatalogoDto("Mogolla de Trigo", 2500.0, "$2.500 / lb", "GRANEL", "Libra", "Granos", "Fresco"),
            new ProductoCatalogoDto("Sal Mineralizada", 2800.0, "$2.800 / lb", "GRANEL", "Libra", "Ganadería", "Minerales")
    );

    public static final List<String> CONCENTRADOS_BULTOS = CATALOGO_BULTOS.stream()
            .map(ProductoCatalogoDto::nombre)
            .toList();

    public static final List<String> PRODUCTOS_GRANEL = CATALOGO_GRANEL.stream()
            .map(ProductoCatalogoDto::nombre)
            .toList();

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

    public List<ProductoCatalogoDto> listarConcentradosDetallados() {
        return CATALOGO_BULTOS;
    }

    public List<ProductoCatalogoDto> listarGranelDetallados() {
        return CATALOGO_GRANEL;
    }

    public Double getCostoDomicilioFijo() {
        return COSTO_DOMICILIO_BUGA;
    }

    @PostConstruct
    @Transactional
    public void repararPreciosAlIniciar() {
        try {
            repararPreciosPedidosHistoricos();
        } catch (Exception ignored) {
        }
    }

    /**
     * Repara cualquier pedido o registro de historial antiguo que haya quedado con total nulo o en 0.
     */
    @Transactional
    public void repararPreciosPedidosHistoricos() {
        List<HistorialVenta> historiales = historialVentaRepository.findAll();
        for (HistorialVenta h : historiales) {
            if (h.getTotal() == null || h.getTotal() <= 0.0) {
                Double sub = estimarSubtotalDesdeTexto(h.getProducto(), h.getCantidad());
                h.setSubtotal(sub);
                h.setCostoEnvio(COSTO_DOMICILIO_BUGA);
                h.setTotal(sub + COSTO_DOMICILIO_BUGA);
                historialVentaRepository.save(h);
            }
        }

        List<Pedido> pedidos = pedidoRepository.findAll();
        for (Pedido p : pedidos) {
            if (p.getTotal() == null || p.getTotal() <= 0.0) {
                double sub = 0.0;
                if (p.getItems() != null && !p.getItems().isEmpty()) {
                    sub = p.getItems().stream()
                            .mapToDouble(i -> i.getSubtotal() != null ? i.getSubtotal() : 0.0)
                            .sum();
                }
                if (sub <= 0.0) {
                    sub = estimarSubtotalDesdeTexto(p.getProducto(), p.getCantidad());
                }
                p.setSubtotal(sub);
                p.setCostoEnvio(COSTO_DOMICILIO_BUGA);
                p.setTotal(sub + COSTO_DOMICILIO_BUGA);
                pedidoRepository.save(p);
            }
        }
    }

    @Transactional
    public List<PedidoResponse> listarPorEstado(EstadoPedido estado) {
        List<Pedido> pedidos = pedidoRepository.findByEstadoOrderByFechaCreacionAsc(estado);
        for (Pedido p : pedidos) {
            if (p.getTotal() == null || p.getTotal() <= 0.0) {
                double sub = estimarSubtotalDesdeTexto(p.getProducto(), p.getCantidad());
                p.setSubtotal(sub);
                p.setCostoEnvio(COSTO_DOMICILIO_BUGA);
                p.setTotal(sub + COSTO_DOMICILIO_BUGA);
                pedidoRepository.save(p);
            }
        }
        return pedidos.stream()
                .map(PedidoResponse::desde)
                .toList();
    }

    @Transactional
    public List<HistorialVenta> listarHistorial() {
        List<HistorialVenta> lista = historialVentaRepository.findAllByOrderByFechaEntregaDesc();
        for (HistorialVenta h : lista) {
            if (h.getTotal() == null || h.getTotal() <= 0.0) {
                Double sub = estimarSubtotalDesdeTexto(h.getProducto(), h.getCantidad());
                h.setSubtotal(sub);
                h.setCostoEnvio(COSTO_DOMICILIO_BUGA);
                h.setTotal(sub + COSTO_DOMICILIO_BUGA);
                historialVentaRepository.save(h);
            }
        }
        return lista;
    }

    /**
     * Resumen de cierre de caja para el día especificado.
     */
    @Transactional
    public ResumenCierreDto calcularCierre(LocalDate fecha) {
        LocalDate f = fecha != null ? fecha : LocalDate.now();
        List<HistorialVenta> todas = historialVentaRepository.findAll();
        List<HistorialVenta> delDia = todas.stream()
                .filter(h -> h.getFechaEntrega() != null && (
                        h.getFechaEntrega().toLocalDate().isEqual(f) ||
                        h.getFechaEntrega().toLocalDate().isEqual(f.minusDays(1))
                ))
                .toList();

        List<HistorialVenta> base = delDia.isEmpty() ? todas : delDia;

        int totalPedidos = base.size();
        double totalRecaudado = 0.0;
        int pedidosEfectivo = 0;
        double totalEfectivo = 0.0;
        int pedidosTransferencia = 0;
        double totalTransferencia = 0.0;

        for (HistorialVenta h : base) {
            double totalVenta = (h.getTotal() != null && h.getTotal() > 0)
                    ? h.getTotal()
                    : (estimarSubtotalDesdeTexto(h.getProducto(), h.getCantidad()) + COSTO_DOMICILIO_BUGA);

            totalRecaudado += totalVenta;

            if ("TRANSFERENCIA".equalsIgnoreCase(h.getMetodoPago())) {
                pedidosTransferencia++;
                totalTransferencia += totalVenta;
            } else {
                pedidosEfectivo++;
                totalEfectivo += totalVenta;
            }
        }

        return new ResumenCierreDto(
                totalPedidos,
                totalRecaudado,
                formatDinero(totalRecaudado),
                pedidosEfectivo,
                totalEfectivo,
                formatDinero(totalEfectivo),
                pedidosTransferencia,
                totalTransferencia,
                formatDinero(totalTransferencia)
        );
    }

    /**
     * Cliente: crea el pedido en estado PENDIENTE calculando precios reales y domicilio.
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

        // Procesar ítems (carrito multi-producto) y calcular subtotal
        List<PedidoItem> itemsAGuardar = new ArrayList<>();
        double subtotalAcumulado = 0.0;

        if (req.items() != null && !req.items().isEmpty()) {
            for (PedidoItemDto itemDto : req.items()) {
                if (itemDto.producto() == null || itemDto.producto().isBlank()) continue;

                String prod = itemDto.producto().trim();
                String tipo = (itemDto.tipoUnidad() != null && !itemDto.tipoUnidad().isBlank())
                        ? itemDto.tipoUnidad().trim().toUpperCase() : "BULTO";
                Double cant = itemDto.cantidad() != null && itemDto.cantidad() > 0 ? itemDto.cantidad() : 1.0;
                Double valor = itemDto.valorDinero();

                Double precioUnit = buscarPrecioUnitario(prod, tipo);
                Double itemSubtotal = 0.0;

                if ("VALOR".equals(tipo)) {
                    itemSubtotal = valor != null && valor > 0 ? valor : 10000.0;
                } else if ("KILO".equals(tipo)) {
                    itemSubtotal = precioUnit * 2 * cant;
                } else {
                    itemSubtotal = precioUnit * cant;
                }

                // Si el frontend ya envió subtotal validado, respetarlo si coincide razonablemente
                if (itemDto.subtotal() != null && itemDto.subtotal() > 0) {
                    itemSubtotal = itemDto.subtotal();
                }

                subtotalAcumulado += itemSubtotal;

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

                PedidoItem item = new PedidoItem(prod, tipo, cant, valor, detalle, precioUnit, itemSubtotal);
                itemsAGuardar.add(item);
                p.agregarItem(item);
            }
        }

        // Si no mandó lista de items pero mandó producto único (soporte formularios legados)
        if (itemsAGuardar.isEmpty()) {
            String prodUnico = req.producto() != null ? req.producto().trim() : "Chunky Perros Adultos Pollo - 25 kg";
            int cantUnica = (req.cantidad() != null && req.cantidad() > 0) ? req.cantidad() : 1;
            Double precioUnit = buscarPrecioUnitario(prodUnico, "BULTO");
            Double itemSubtotal = precioUnit * cantUnica;
            subtotalAcumulado += itemSubtotal;

            PedidoItem item = new PedidoItem(prodUnico, "BULTO", (double) cantUnica, null,
                    cantUnica + " bulto(s) de " + prodUnico, precioUnit, itemSubtotal);
            itemsAGuardar.add(item);
            p.agregarItem(item);
        }

        // Totales comerciales
        p.setSubtotal(subtotalAcumulado);
        p.setCostoEnvio(COSTO_DOMICILIO_BUGA);
        p.setTotal(subtotalAcumulado + COSTO_DOMICILIO_BUGA);

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
     * Calcula el subtotal estimado a partir del texto del producto y cantidad,
     * para reparar pedidos históricos que quedaron con total = 0.
     */
    public Double estimarSubtotalDesdeTexto(String textoProducto, Integer cantidadTotal) {
        if (textoProducto == null || textoProducto.isBlank()) {
            return 64000.0;
        }

        double subtotal = 0.0;
        String[] partes = textoProducto.split("\\+");

        for (String parte : partes) {
            String p = parte.trim();
            if (p.isEmpty()) continue;

            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?i)^(\\d+(?:\\.\\d+)?)\\s*(bulto\\(?s?\\)?|kg|kilo\\(?s?\\)?|lb|libra\\(?s?\\)?|\\$)?\\s*(?:de)?\\s*(.+)$").matcher(p);

            double cant = 1.0;
            String unidad = "BULTO";
            String nombreProd = p;

            if (m.find()) {
                try {
                    cant = Double.parseDouble(m.group(1));
                } catch (Exception ignored) {}
                String u = m.group(2) != null ? m.group(2).toLowerCase() : "";
                if (u.contains("kg") || u.contains("kilo")) {
                    unidad = "KILO";
                } else if (u.contains("lb") || u.contains("libra")) {
                    unidad = "LIBRA";
                } else if (u.contains("$")) {
                    unidad = "VALOR";
                } else {
                    unidad = "BULTO";
                }
                if (m.group(3) != null && !m.group(3).isBlank()) {
                    nombreProd = m.group(3).trim();
                }
            }

            Double precioUnit = buscarPrecioUnitario(nombreProd, "BULTO".equals(unidad) ? "BULTO" : "GRANEL");

            if ("KILO".equals(unidad)) {
                // 1 kg = 2 libras
                subtotal += (precioUnit * 2 * cant);
            } else if ("LIBRA".equals(unidad)) {
                subtotal += (precioUnit * cant);
            } else {
                subtotal += (precioUnit * cant);
            }
        }

        if (subtotal <= 0.0) {
            int cant = (cantidadTotal != null && cantidadTotal > 0) ? cantidadTotal : 1;
            subtotal = 64000.0 * cant;
        }

        return subtotal;
    }

    /**
     * Búsqueda de precio unitario en catálogo con coincidencia flexible y tolerancia a acentos/guiones.
     */
    public Double buscarPrecioUnitario(String producto, String tipoUnidad) {
        if (producto == null) return "BULTO".equalsIgnoreCase(tipoUnidad) ? 128000.0 : 2800.0;

        String prodNorm = producto.toLowerCase()
                .replace('–', '-')
                .replace('—', '-')
                .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u");

        if ("BULTO".equalsIgnoreCase(tipoUnidad)) {
            if (prodNorm.contains("chunky")) return 148000.0;
            if (prodNorm.contains("dog chow") || prodNorm.contains("purina")) return 156000.0;
            if (prodNorm.contains("ringo")) return 128000.0;
            if (prodNorm.contains("mirringo")) return 69000.0;
            if (prodNorm.contains("donkat")) return 89000.0;
            if (prodNorm.contains("ponedora") || prodNorm.contains("italcol")) return 98000.0;
            if (prodNorm.contains("engorde") || prodNorm.contains("solla")) return 106000.0;
            if (prodNorm.contains("monello")) return 119000.0;
            if (prodNorm.contains("cat chow")) return 134000.0;

            for (ProductoCatalogoDto b : CATALOGO_BULTOS) {
                String nomCat = b.nombre().toLowerCase().replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u");
                if (prodNorm.contains(nomCat.substring(0, Math.min(8, nomCat.length())))) {
                    return b.precio();
                }
            }
            return 128000.0; // Precio promedio de bulto en caso de producto no listado
        } else {
            if (prodNorm.contains("alpiste")) return 3500.0;
            if (prodNorm.contains("partido")) return 2400.0;
            if (prodNorm.contains("maiz") || prodNorm.contains("amarillo")) return 2200.0;
            if (prodNorm.contains("canario")) return 4200.0;
            if (prodNorm.contains("q-ida") || prodNorm.contains("cida")) return 3800.0;
            if (prodNorm.contains("trigo") || prodNorm.contains("mogolla")) return 2500.0;
            if (prodNorm.contains("sal")) return 2800.0;

            for (ProductoCatalogoDto g : CATALOGO_GRANEL) {
                String nomCat = g.nombre().toLowerCase().replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u");
                if (prodNorm.contains(nomCat)) {
                    return g.precio();
                }
            }
            return 2800.0; // Precio promedio por libra
        }
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

    private static String formatDinero(Double valor) {
        if (valor == null) return "$0";
        return String.format("$%,.0f", valor);
    }
}
