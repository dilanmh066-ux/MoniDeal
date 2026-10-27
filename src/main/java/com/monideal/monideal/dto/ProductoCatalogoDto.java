package com.monideal.monideal.dto;

/**
 * Producto del catálogo con información detallada de presentación, categoría,
 * precio comercial en Buga y fotografía representativa.
 */
public record ProductoCatalogoDto(
        String nombre,
        Double precio,
        String precioFormateado,
        String tipo,       // "BULTO" o "GRANEL"
        String unidad,     // "Bulto 25 kg", "libra", "kilo", etc.
        String categoria,  // "Perros Adultos", "Cachorros", "Gatos Adultos", "Gatitos", "Avicultura", "Porcicultura", "Ganadería", "Piscicultura", "Granos"
        String etiqueta,   // Badge ej: "Más vendido", "Económico", "Calidad Pro"
        String imagenUrl
) {
    /**
     * Constructor sobrecargado para retrocompatibilidad en llamadas sin imagen explícita.
     */
    public ProductoCatalogoDto(String nombre, Double precio, String precioFormateado, String tipo, String unidad, String categoria, String etiqueta) {
        this(nombre, precio, precioFormateado, tipo, unidad, categoria, etiqueta, "");
    }
}
