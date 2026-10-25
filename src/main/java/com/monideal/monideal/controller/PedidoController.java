package com.monideal.monideal.controller;

import com.monideal.monideal.dto.HistorialVentaResponse;
import com.monideal.monideal.dto.PedidoRequest;
import com.monideal.monideal.dto.PedidoResponse;
import com.monideal.monideal.dto.ResumenCierreDto;
import com.monideal.monideal.model.EstadoPedido;
import com.monideal.monideal.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Monideal.
 * - Rutas de vistas (devuelven HTML de Thymeleaf): /pedido, /caja, /rutas, /historial
 * - Rutas de API (devuelven JSON, se llaman con Fetch): /api/pedidos/**, /api/historial
 */
@Controller
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // =====================================================================
    // VISTAS (Thymeleaf)
    // =====================================================================

    @GetMapping("/")
    public String inicio() {
        return "redirect:/pedido";
    }

    /** Pantalla del cliente: este es el link que se envía por WhatsApp (soporta /pedido y /cliente). */
    @GetMapping({"/pedido", "/cliente"})
    public String vistaCliente(Model model) {
        model.addAttribute("concentrados", pedidoService.listarConcentrados());
        model.addAttribute("granel", pedidoService.listarGranel());
        model.addAttribute("catalogoBultos", pedidoService.listarConcentradosDetallados());
        model.addAttribute("catalogoGranel", pedidoService.listarGranelDetallados());
        model.addAttribute("costoDomicilio", pedidoService.getCostoDomicilioFijo());
        return "cliente";
    }

    /** Pantalla de la cajera y punto de venta. */
    @GetMapping("/caja")
    public String vistaCaja(Model model) {
        List<PedidoResponse> pendientes = pedidoService.listarPorEstado(EstadoPedido.PENDIENTE);
        List<PedidoResponse> enRuta = pedidoService.listarPorEstado(EstadoPedido.EN_RUTA);
        ResumenCierreDto cierre = pedidoService.calcularCierre(null);

        model.addAttribute("pedidos", pendientes);
        model.addAttribute("totalPendientes", pendientes.size());
        model.addAttribute("totalEnRuta", enRuta.size());
        model.addAttribute("cierreHoy", cierre);
        return "caja";
    }

    /** Pantalla de rutas para los domiciliarios. */
    @GetMapping("/rutas")
    public String vistaRutas(Model model) {
        List<PedidoResponse> enRuta = pedidoService.listarPorEstado(EstadoPedido.EN_RUTA);
        model.addAttribute("pedidos", enRuta);
        model.addAttribute("totalEnRuta", enRuta.size());

        double efectivoRutas = enRuta.stream()
                .filter(p -> !"TRANSFERENCIA".equalsIgnoreCase(p.getMetodoPago()))
                .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0)
                .sum();
        model.addAttribute("efectivoRutasTotal", efectivoRutas);
        model.addAttribute("efectivoRutasFormateado", String.format("$%,.0f", efectivoRutas));
        return "rutas";
    }

    /** Pantalla de historial de ventas, cierre de caja y rutas completadas. */
    @GetMapping("/historial")
    public String vistaHistorial(Model model) {
        model.addAttribute("historial", pedidoService.listarHistorial());
        model.addAttribute("cierreHoy", pedidoService.calcularCierre(null));
        return "historial";
    }

    // =====================================================================
    // API JSON (Fetch API)
    // =====================================================================

    /** Lista pedidos por estado. Ej: GET /api/pedidos?estado=PENDIENTE */
    @GetMapping("/api/pedidos")
    @ResponseBody
    public List<PedidoResponse> listar(@RequestParam EstadoPedido estado) {
        return pedidoService.listarPorEstado(estado);
    }

    /** Lista de ventas entregadas para la pantalla de historial o reportes. */
    @GetMapping("/api/historial")
    @ResponseBody
    public List<HistorialVentaResponse> apiHistorial() {
        return pedidoService.listarHistorial()
                .stream()
                .map(HistorialVentaResponse::desde)
                .toList();
    }

    /** Cierre de caja en vivo para reportes de administración. */
    @GetMapping("/api/cierre")
    @ResponseBody
    public ResumenCierreDto apiCierre() {
        return pedidoService.calcularCierre(null);
    }

    /** Cliente: crea un pedido (queda PENDIENTE). */
    @PostMapping("/api/pedidos")
    @ResponseBody
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(request));
    }

    /** Oficina / Cajera: confirma el pago de una transferencia bancaria. */
    @PutMapping("/api/pedidos/{id}/confirmar-pago")
    @ResponseBody
    public PedidoResponse confirmarPago(@PathVariable Long id) {
        return pedidoService.confirmarPago(id);
    }

    /** Cajera: asigna a domicilio (PENDIENTE -> EN_RUTA). */
    @PutMapping("/api/pedidos/{id}/en-ruta")
    @ResponseBody
    public PedidoResponse asignarADomicilio(@PathVariable Long id) {
        return pedidoService.asignarADomicilio(id);
    }

    /** Domiciliario: confirma la entrega (EN_RUTA -> ENTREGADO + historial). */
    @PutMapping("/api/pedidos/{id}/entregado")
    @ResponseBody
    public PedidoResponse confirmarEntrega(@PathVariable Long id) {
        return pedidoService.confirmarEntrega(id);
    }
}
