-- ============================================================================
-- MONIDEAL - Script de base de datos
-- Motor: MySQL 8.0+ / MariaDB | Charset: utf8mb4
-- Flujo de estados: PENDIENTE -> EN_RUTA -> ENTREGADO
-- ============================================================================

CREATE DATABASE IF NOT EXISTS monideal_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE monideal_db;

-- ----------------------------------------------------------------------------
-- Tabla: pedidos
-- Guarda el pedido del cliente (Bultos o Granel en Guadalajara de Buga)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pedidos (
    id_pedido         BIGINT        NOT NULL AUTO_INCREMENT,

    -- Datos de entrega del cliente en Buga
    cliente_nombre    VARCHAR(120)  NOT NULL,
    cliente_telefono  VARCHAR(20)   NOT NULL,
    direccion         VARCHAR(200)  NOT NULL,
    barrio            VARCHAR(100)  NOT NULL,
    indicaciones      VARCHAR(300)  NULL,

    -- Resumen de productos y cantidad
    producto          VARCHAR(500)  NOT NULL,
    cantidad          INT           NOT NULL DEFAULT 1,

    -- Método de pago y validación de oficina
    metodo_pago       VARCHAR(30)   NOT NULL DEFAULT 'EFECTIVO',
    pago_confirmado   BOOLEAN       NOT NULL DEFAULT TRUE,
    paga_con          VARCHAR(50)   NULL,

    -- Estado y trazabilidad
    estado            ENUM('PENDIENTE','EN_RUTA','ENTREGADO') NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_asignacion  DATETIME      NULL,
    fecha_entrega     DATETIME      NULL,

    PRIMARY KEY (id_pedido)
) ENGINE = InnoDB;

CREATE INDEX idx_pedidos_estado_fecha ON pedidos (estado, fecha_creacion);

-- ----------------------------------------------------------------------------
-- Tabla: pedido_items
-- Carrito multi-producto: desglose de bultos, libras o monto en dinero
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pedido_items (
    id_item           BIGINT        NOT NULL AUTO_INCREMENT,
    id_pedido         BIGINT        NOT NULL,
    producto          VARCHAR(150)  NOT NULL,
    tipo_unidad       VARCHAR(30)   NOT NULL DEFAULT 'BULTO',
    cantidad          DOUBLE        NULL DEFAULT 1.0,
    valor_dinero      DOUBLE        NULL,
    detalle           VARCHAR(120)  NOT NULL,

    PRIMARY KEY (id_item),
    CONSTRAINT fk_pedido_items_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos (id_pedido) ON DELETE CASCADE
) ENGINE = InnoDB;

-- ----------------------------------------------------------------------------
-- Tabla: historial_ventas
-- Auditoría inmutable de entregas confirmadas por los domiciliarios
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS historial_ventas (
    id_historial      BIGINT        NOT NULL AUTO_INCREMENT,
    id_pedido         BIGINT        NOT NULL,
    cliente_nombre    VARCHAR(120)  NOT NULL,
    cliente_telefono  VARCHAR(20)   NOT NULL,
    direccion         VARCHAR(200)  NOT NULL,
    barrio            VARCHAR(100)  NOT NULL,
    producto          VARCHAR(500)  NOT NULL,
    cantidad          INT           NOT NULL,
    metodo_pago       VARCHAR(30)   NULL,
    paga_con          VARCHAR(50)   NULL,
    fecha_pedido      DATETIME      NOT NULL,
    fecha_entrega     DATETIME      NOT NULL,

    PRIMARY KEY (id_historial),
    CONSTRAINT uk_historial_pedido UNIQUE (id_pedido),
    CONSTRAINT fk_historial_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos (id_pedido)
) ENGINE = InnoDB;

CREATE INDEX idx_historial_fecha_entrega ON historial_ventas (fecha_entrega);
