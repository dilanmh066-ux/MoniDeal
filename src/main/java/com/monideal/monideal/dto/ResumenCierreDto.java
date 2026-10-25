package com.monideal.monideal.dto;

/**
 * Resumen de Cierre de Caja Diario para la gerencia y punto de venta.
 * Desglosa ventas por método de pago y totales recaudados.
 */
public record ResumenCierreDto(
        int totalPedidos,
        double totalRecaudado,
        String totalRecaudadoFormateado,
        int pedidosEfectivo,
        double totalEfectivo,
        String totalEfectivoFormateado,
        int pedidosTransferencia,
        double totalTransferencia,
        String totalTransferenciaFormateado
) {
    public static ResumenCierreDto vacio() {
        return new ResumenCierreDto(0, 0.0, "$0", 0, 0.0, "$0", 0, 0.0, "$0");
    }
}
