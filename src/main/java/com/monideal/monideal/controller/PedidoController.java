package com.monideal.monideal.controller;

import com.monideal.monideal.dto.PedidoRequest;
import com.monideal.monideal.dto.PedidoResponse;
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
 * - Rutas de vistas (devuelven HTML de Thymeleaf): /pedido, /caja, /rutas
 * - Rutas de API (devuelven JSON, se llaman con Fetch): /api/pedidos/**
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

    /** Pantalla del cliente: este es el link que se envía por WhatsApp. */
    @GetMapping("/pedido")
    public String vistaCliente(Model model) {
        model.addAttribute("concentrados", pedidoService.listarConcentrados());
        return "cliente";
    }

    /** Pantalla de la cajera. */
    @GetMapping("/caja")
    public String vistaCaja(Model model) {
        model.addAttribute("pedidos", pedidoService.listarPorEstado(EstadoPedido.PENDIENTE));
        return "caja";
    }

    /** Pantalla de rutas para los domiciliarios. */
    @GetMapping("/rutas")
    public String vistaRutas(Model model) {
        model.addAttribute("pedidos", pedidoService.listarPorEstado(EstadoPedido.EN_RUTA));
        return "rutas";
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

    /** Cliente: crea un pedido (queda PENDIENTE). */
    @PostMapping("/api/pedidos")
    @ResponseBody
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(request));
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
