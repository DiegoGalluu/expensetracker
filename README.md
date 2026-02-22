# ExpenseTracker - Gestor de Finanzas Personales

## Descripción

ExpenseTracker es una aplicación Android nativa desarrollada como proyecto final del módulo de Programación Multimedia y Dispositivos Móviles (PMDM) y Programación de Servicios y Procesos (PSP). La aplicación permite a los usuarios gestionar sus finanzas personales de manera sencilla, registrando gastos e ingresos, organizándolos por categorías, estableciendo presupuestos y visualizando resúmenes mediante gráficos.

## Tecnologías Utilizadas

### Frontend
- Jetpack Compose - UI moderna y declarativa
- Material Design 3 - Sistema de diseño
- Navigation Compose - Navegación entre pantallas
- Kotlin - Lenguaje de programación

### Arquitectura
- MVVM (Model-View-ViewModel) - Patrón arquitectónico
- Repository Pattern - Abstracción de fuentes de datos
- StateFlow - Gestión reactiva del estado
- Coroutines - Programación asíncrona

### Persistencia
- Room Database - ORM para base de datos local
- SQLite - Motor de base de datos

### Servicios Remotos
- Retrofit2 - Cliente HTTP para consumo de APIs
- Gson - Serialización y deserialización JSON
- OkHttp - Cliente HTTP subyacente

### Autenticación
- Auth0 - Sistema de autenticación y gestión de identidad

## Estructura del Proyecto

```
app/src/main/java/com/dam/expensetracker/
├── datos/
│   ├── local/
│   │   ├── entidades/          # Entidades de Room (Transaccion, Categoria, Cuenta, Presupuesto)
│   │   ├── dao/                # DAOs para operaciones CRUD
│   │   └── base/               # BaseDatosFinanzas (clase principal de Room)
│   ├── remoto/
│   │   ├── api/                # Interfaces de Retrofit (ApiDivisas)
│   │   └── dto/                # Objetos de transferencia de datos
│   └── repositorios/           # Repositorios que abstraen los datos
├── ui/
│   ├── pantallas/
│   │   ├── login/              # Pantalla de login con Auth0
│   │   ├── inicio/             # Dashboard principal
│   │   ├── detalle/            # Detalle de transacción
│   │   └── formulario/         # Formulario crear/editar transacción
│   ├── componentes/            # Componentes reutilizables (botones, tarjetas, gráficos)
│   ├── navegacion/             # Sistema de navegación con NavHost
│   └── tema/                   # Colores, tipografía y tema de la app
├── utilidades/                 # Clases de utilidad (GestorAuth, Constantes)
└── MainActivity.kt             # Actividad principal
```

## Cumplimiento de Requisitos

### Jetpack Compose (RA3 PMDM)
- UI 100% en Compose sin XML
- Navegación con Navigation Compose
- 4 pantallas principales: Login, Inicio, Detalle, Formulario
- Estados reactivos con StateFlow
- Componentes reutilizables (BotonPrincipal, TarjetaTransaccion, GraficoCircular)
- Tema personalizado con Material Design 3
- Listas con LazyColumn
- Formularios con validación

### Arquitectura MVVM (RA4 PMDM)
- Separación clara de capas: View (Composables), ViewModel, Repository
- ViewModels con StateFlow para exponer estado
- Repository pattern para abstraer fuentes de datos
- Manejo correcto del ciclo de vida
- Uso de Coroutines para operaciones asíncronas

### Persistencia con Room (RA5 PMDM)
- 4 entidades: Transaccion, Categoria, Cuenta, Presupuesto
- DAOs con operaciones CRUD completas
- Uso de Flow para observar cambios
- Consultas @Query con filtrado por categoría y mes
- Relaciones entre entidades con ForeignKey
- Datos iniciales precargados

### Retrofit2 (RA4 PSP)
- Integración con API pública de tipos de cambio (exchangerate-api.com)
- 2 endpoints: obtener tipos de cambio base y tipos del dólar
- Serialización JSON con Gson
- Manejo de errores con try-catch
- DTOs para mapear respuestas
- Estados de UI: cargando, éxito, error

### Auth0 (RA5 PSP)
- Pantalla de login con botón de Auth0
- Manejo de tokens y credenciales
- Logout funcional
- Protección de pantallas principales
- Muestra del email del usuario autenticado
- Callback handling para OAuth

## Instalación y Ejecución

### Requisitos Previos
- Android Studio Hedgehog o superior
- JDK 17
- SDK mínimo: Android 7.0 (API 24)
- SDK objetivo: Android 14 (API 34)

### Pasos de Instalación

1. Clonar el repositorio:
```bash
git clone https://github.com/tu-usuario/expensetracker.git
cd expensetracker
```

2. Abrir el proyecto en Android Studio:
   - File > Open
   - Seleccionar la carpeta del proyecto
   - Esperar a que Gradle sincronice las dependencias

3. Configurar Auth0:
   - Crear una cuenta gratuita en https://auth0.com
   - Crear una aplicación de tipo "Native"
   - Copiar el Domain y Client ID
   - Abrir el archivo `GestorAuth.kt`
   - Reemplazar los valores de ejemplo:
     ```kotlin
     private val DOMINIO = "tu-dominio.eu.auth0.com"
     private val CLIENT_ID = "tu_client_id_aqui"
     ```
   - En el dashboard de Auth0, configurar:
     - Callback URLs: `com.dam.expensetracker://tu-dominio.auth0.com/android/com.dam.expensetracker/callback`
     - Logout URLs: `com.dam.expensetracker://tu-dominio.auth0.com/android/com.dam.expensetracker/callback`

4. Ejecutar la aplicación:
   - Conectar un dispositivo Android o iniciar un emulador
   - Click en el botón Run (triángulo verde)
   - La app se instalará y abrirá automáticamente

### Nota sobre las APIs

La aplicación utiliza la API pública de tipos de cambio https://api.exchangerate-api.com que no requiere API key. Si la API no está disponible, la funcionalidad de conversión de divisas simplemente no mostrará datos, pero el resto de la aplicación funcionará normalmente.

## Funcionalidades Principales

### Gestión de Transacciones
- Registrar gastos e ingresos con cantidad, categoría, cuenta y nota
- Editar transacciones existentes
- Eliminar transacciones con confirmación
- Ver listado completo de transacciones ordenadas por fecha

### Categorías y Cuentas
- 7 categorías predefinidas con colores: Comida, Transporte, Ocio, Salud, Vivienda, Educación, Otros
- 2 cuentas por defecto: Efectivo y Banco
- Presupuestos mensuales de 500€ por categoría (configurables)

### Resumen Financiero
- Cálculo automático del saldo total (ingresos - gastos)
- Total de gastos del mes actual
- Gráfico circular mostrando distribución de gastos por categoría
- Indicadores visuales diferenciando gastos (rojo) e ingresos (verde)

### Conversión de Divisas
- Consulta de tipos de cambio actuales (EUR, USD, etc.)
- Actualización automática desde API externa
- Manejo de errores cuando no hay conexión

## Problemas Conocidos y Soluciones

### Auth0
Si el login de Auth0 no funciona, verificar:
- Las credenciales están correctamente configuradas en `GestorAuth.kt`
- Las URLs de callback están configuradas en el dashboard de Auth0
- El dispositivo/emulador tiene conexión a Internet

### Base de Datos
Si la app se cierra al iniciar:
- Desinstalar la app del dispositivo
- Volver a ejecutar desde Android Studio
- Room recreará la base de datos limpia

### API de Divisas
Si los tipos de cambio no se muestran:
- Verificar conexión a Internet
- La funcionalidad es opcional y no afecta el resto de la app

## Decisiones de Diseño

### Por qué MVVM sin Hilt
Se decidió no utilizar Hilt (inyección de dependencias) para mantener el código más sencillo y comprensible para un alumno de DAM. Se crearon factories manuales para los ViewModels que, aunque más verbosas, son más fáciles de entender.

### Repositorio Único vs Múltiples
Se usó un único `RepositorioFinanzas` que agrupa todos los DAOs en lugar de un repositorio por entidad. Esto simplifica la arquitectura sin perder funcionalidad.

### Gráfico Personalizado
Se implementó un gráfico circular simple con Canvas en lugar de usar librerías externas pesadas. Esto reduce dependencias y muestra dominio de Compose.

### Datos Iniciales
La base de datos se puebla automáticamente con categorías, cuentas y presupuestos por defecto en el primer inicio. Esto mejora la experiencia del usuario que puede probar la app inmediatamente.

## Mejoras Futuras

Si se dispusiera de más tiempo, se podrían implementar:
- Filtros avanzados de transacciones por fecha, categoría, cuenta
- Exportación de datos a CSV o PDF
- Recordatorios para pagos recurrentes
- Soporte para múltiples idiomas
- Modo oscuro automático según hora del día
- Sincronización en la nube
- Notificaciones cuando se excede un presupuesto

## Créditos

Proyecto realizado por un alumno de 2º de Desarrollo de Aplicaciones Multiplataforma (DAM) como parte de la evaluación de los módulos PMDM y PSP del segundo trimestre.

Curso: 2025-2026

## Licencia

Este proyecto es de uso educativo. Se permite su uso, modificación y distribución con fines académicos.
