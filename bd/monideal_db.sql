-- ============================================================================
-- MONIDEAL - Script inicial de base de datos
-- Motor: MySQL 8.0+ | Charset: utf8mb4
-- Flujo de estados: PENDIENTE -> EN_RUTA -> ENTREGADO
-- ============================================================================

CREATE DATABASE IF NOT EXISTS monideal_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE monideal_db;

-- ----------------------------------------------------------------------------
-- Tabla: pedidos
-- Guarda el pedido que hace el cliente desde la web (link de WhatsApp).
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pedidos (
    id_pedido         BIGINT        NOT NULL AUTO_INCREMENT,

    -- Datos de entrega del cliente
    cliente_nombre    VARCHAR(120)  NOT NULL,
    cliente_telefono  VARCHAR(20)   NOT NULL,
    direccion         VARCHAR(200)  NOT NULL,
    barrio            VARCHAR(100)  NOT NULL,
    indicaciones      VARCHAR(300)  NULL,

    -- Producto solicitado
    producto          VARCHAR(150)  NOT NULL,
    cantidad          INT           NOT NULL DEFAULT 1,

    -- Estado y trazabilidad
    estado            ENUM('PENDIENTE','EN_RUTA','ENTREGADO') NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_asignacion  DATETIME      NULL,
    fecha_entrega     DATETIME      NULL,

    PRIMARY KEY (id_pedido),
    CONSTRAINT chk_pedidos_cantidad CHECK (cantidad BETWEEN 1 AND 50)
) ENGINE = InnoDB;

-- Índice para que las pantallas de Caja y Rutas filtren rápido por estado
CREATE INDEX idx_pedidos_estado_fecha ON pedidos (estado, fecha_creacion);

-- ----------------------------------------------------------------------------
-- Tabla: historial_ventas
-- Cuando el domiciliario confirma la entrega, se guarda una copia aquí.
-- Así el historial no cambia aunque después se modifique el pedido.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS historial_ventas (
    id_historial      BIGINT        NOT NULL AUTO_INCREMENT,
    id_pedido         BIGINT        NOT NULL,
    cliente_nombre    VARCHAR(120)  NOT NULL,
    cliente_telefono  VARCHAR(20)   NOT NULL,
    direccion         VARCHAR(200)  NOT NULL,
    barrio            VARCHAR(100)  NOT NULL,
    producto          VARCHAR(150)  NOT NULL,
    cantidad          INT           NOT NULL,
    fecha_pedido      DATETIME      NOT NULL,
    fecha_entrega     DATETIME      NOT NULL,

    PRIMARY KEY (id_historial),
    CONSTRAINT uk_historial_pedido UNIQUE (id_pedido),
    CONSTRAINT fk_historial_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos (id_pedido)
) ENGINE = InnoDB;

CREATE INDEX idx_historial_fecha_entrega ON historial_ventas (fecha_entrega);
