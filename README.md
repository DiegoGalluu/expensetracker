# ExpenseTracker

ExpenseTracker es una app Android para llevar tus finanzas personales de forma simple: anotas ingresos y gastos, organizas por categorías/cuentas y ves un resumen claro de cómo va tu mes.

Este README está pensado para entender **cómo se usa la app** y cuál es su flujo principal, sin entrar en detalles técnicos innecesarios.

---

## ¿Qué puedes hacer en la app?

- Iniciar sesión con Auth0 y mantener sesión activa.
- Registrar transacciones (gasto o ingreso).
- Editar y borrar movimientos.
- Gestionar categorías personalizadas y cuentas.
- Definir metas y presupuestos.
- Configurar transacciones recurrentes.
- Consultar conversión de divisas.
- Ver movimientos de una API bancaria simulada.
- Exportar tus datos a CSV o PDF.

---

## Flujo de uso (paso a paso)

### 1) Login
Al abrir la app, entras por la pantalla de inicio de sesión.

- Si ya había sesión válida, la app entra directamente.
- Si no, inicias sesión con Auth0.

### 2) Pantalla principal (Inicio)
Después del login llegas al dashboard:

- Saldo total.
- Gastos del mes.
- Gráfico por categorías.
- Lista de transacciones recientes.

Desde aquí puedes abrir el menú lateral para acceder al resto de apartados.

### 3) Crear una transacción
Con el botón `+` creas un movimiento nuevo:

- Eliges si es gasto o ingreso.
- Indicas cantidad, categoría, cuenta y nota.
- Guardas y vuelves al inicio.

Extra útil: en el selector de categoría puedes crear una nueva categoría al momento, sin salir del formulario.

### 4) Revisar y editar
Desde la lista puedes entrar al detalle de una transacción para:

- Ver la información completa.
- Editar datos.
- Eliminar el registro.

### 5) Menú lateral (funciones principales)
En el drawer tienes acceso a:

- **Metas**: objetivos económicos.
- **Presupuestos**: límites de gasto por categoría.
- **Cuentas**: gestión de cuentas (efectivo, banco, etc.).
- **Recurrentes**: cargos/ingresos automáticos con frecuencia.
- **Divisas**: conversión con tipo de cambio actualizado.
- **Banco**: visualización de movimientos de API simulada.
- **Exportar**: generación de CSV y PDF.

---

## Recurrentes: cómo funciona

La sección de recurrentes permite preparar movimientos que se repiten (por ejemplo, alquiler, nómina, suscripciones):

- Puedes crear, editar y eliminar recurrentes.
- Puedes marcar cada recurrente como activo o inactivo.
- Ves un resumen mensual de ingresos fijos y gastos fijos.
- También puedes crear una categoría nueva desde su propio selector.

---

## Sesión y datos por usuario

La app cierra sesión desde el botón junto al email en el menú lateral.

Además, los datos locales están separados por usuario: si entra una cuenta nueva, ve su propio espacio de datos (no se mezclan con otra sesión).

---

## Exportación de datos

En la sección **Exportar** puedes generar:

- **CSV**: útil para hojas de cálculo.
- **PDF**: útil para compartir o archivar.

Los archivos se guardan en Documents para que sea fácil localizarlos.

---

## Requisitos para ejecutar

- Android Studio.
- Dispositivo/emulador Android (API 24 o superior).
- Configuración de Auth0 válida (dominio y client ID).
- Conexión a internet para login y funciones de APIs remotas.

---

## Resumen rápido

ExpenseTracker está pensada para que el flujo diario sea directo:

1. Entras.
2. Registras movimientos.
3. Controlas presupuesto y recurrentes.
4. Revisas resumen.
5. Exportas cuando lo necesites.

Si quieres ampliar la app, el siguiente paso natural sería añadir filtros avanzados por fechas/categorías o estadísticas históricas comparativas por meses.
