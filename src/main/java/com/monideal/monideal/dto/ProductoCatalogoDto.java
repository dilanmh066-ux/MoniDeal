package com.monideal.monideal.dto;

/**
 * Producto del catálogo con información detallada de presentación y precio comercial en Buga.
 */
public record ProductoCatalogoDto(
        String nombre,
        Double precio,
        String precioFormateado,
        String tipo,       // "BULTO" o "GRANEL"
        String unidad,     // "Bulto 25 kg", "libra", "kilo", etc.
        String categoria,  // "Perros", "Gatos", "Aves", "Granos"
        String etiqueta    // Badge ej: "Más vendido", "Económico", "Calidad Pro"
) {}
