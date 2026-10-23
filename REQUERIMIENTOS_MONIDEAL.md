# 📋 MoniDeal — Estado de Implementación y Manual Operativo

Sistema de pedidos a domicilio para concentrados y granos en **Guadalajara de Buga**.

---

## 🚀 Estado de Requerimientos Implementados

### 1. Carrito Multi-producto y Venta a Granel
* **Bultos cerrados:** Selección de concentrados en presentación oficial de saco (Chunky, Dog Chow, Ringo, Mirringo, Donkat, Italcol, Solla, etc.) con selector de cantidad.
* **Venta a granel flexible:** Pestaña dedicada con tres modalidades:
  * **Por Libras:** (ej. 2 lb, 5 lb de alpiste o maíz).
  * **Por Kilos:** (ej. 1 kg, 3 kg).
  * **Por Presupuesto / Dinero ($):** Permite pedir un valor exacto (ej. `$10.000` de alpiste, `$20.000` de maíz).
* **Lista de compras en vivo:** El cliente puede agregar múltiples bultos y productos a granel en el mismo pedido, eliminar ítems antes de enviar y ver su resumen.

---

### 2. Métodos de Pago y Flujo de Oficina vs. Caja
* **Efectivo contra entrega:**
  * El cliente puede indicar con cuánto billete va a pagar (ej. *"Con billete de $50.000"*) para que el domiciliario lleve el cambio exacto.
  * Nace con estado verificado (`🟢 Efectivo contra entrega`).
* **Transferencia bancaria (Nequi / Bancolombia / Daviplata):**
  * Nace con estado: `🟡 Esperando Transferencia`.
  * **Pantalla de éxito del cliente:** Muestra un botón directo a WhatsApp con el mensaje pre-armado:
    > *"Hola Monideal Buga, acabo de registrar el Pedido #X a nombre de Carlos. Productos: ... Aquí adjunto el comprobante."*
  * **Pantalla de Caja / Oficina:** Cuenta con el botón **`✅ Oficina: Confirmar Pago Recibido`**. Al presionarlo, el pedido pasa a `🟢 Transferencia OK`.
  * Si la cajera intenta despachar un pedido sin confirmación de pago, el sistema muestra una advertencia de seguridad.

---

### 3. Geolocalización Estricta en Guadalajara de Buga
* El botón **"🗺️ Cómo llegar"** en la pantalla de Rutas concatena automáticamente:
  `[Dirección], Barrio [Barrio], Guadalajara de Buga, Valle del Cauca, Colombia`
* Elimina el desvío a otras ciudades de Colombia en Google Maps.

---

### 4. Seguridad de Roles (PIN para Caja)
* Al ingresar a `/caja`, el sistema solicita un **PIN de seguridad** (por defecto: `1234`).
* Esto previene que los domiciliarios o clientes manipulen o despachen pedidos sin autorización desde sus teléfonos.
* La sesión queda guardada en el navegador mientras la pestaña esté abierta.

---

### 5. Historial de Entregas y Auditoría (`/historial`)
* Nueva pantalla de **Historial** accesible desde la barra superior de navegación.
* Muestra todos los pedidos entregados por los repartidores.
* Incluye buscador instantáneo por cliente, teléfono, barrio, fecha o producto.
* Registra el método de pago con el que fue cancelado cada pedido.

---

### 6. Alerta Sonora en Caja
* Botón **"🔊 Probar timbre"** en la barra de resumen de Caja para desbloquear el audio en el navegador.
* Campana sonora automática cuando entra un pedido nuevo vía AJAX en tiempo real.

---

## 🌐 URLs del Sistema

| Rol | URL | Función |
| :--- | :--- | :--- |
| **Cliente** | [http://localhost:8080/pedido](http://localhost:8080/pedido) | Hacer pedido (Bultos + Granel + Carrito + WhatsApp) |
| **Caja / Oficina** | [http://localhost:8080/caja](http://localhost:8080/caja) | Revisar pedidos, validar transferencias y despachar (PIN: `1234`) |
| **Domiciliarios** | [http://localhost:8080/rutas](http://localhost:8080/rutas) | Ver rutas activas en Buga, mapa, cobro y confirmar entrega |
| **Auditoría / Dueño** | [http://localhost:8080/historial](http://localhost:8080/historial) | Trazabilidad histórica de ventas y cuadre |
