# Estructura Completa del Proyecto ExpenseTracker

## Árbol de Archivos

```
expensetracker/
│
├── build.gradle.kts                    # Configuración de Gradle con dependencias
├── README.md                           # Documentación principal
├── ESTRUCTURA.md                       # Este archivo
│
└── app/
    ├── build.gradle.kts               # Configuración específica de la app
    └── src/
        └── main/
            ├── AndroidManifest.xml     # Manifest de la aplicación
            ├── res/                    # Recursos (layouts XML no usados, solo para configs)
            └── java/com/dam/expensetracker/
                │
                ├── MainActivity.kt     # Actividad principal
                │
                ├── datos/
                │   ├── local/
                │   │   ├── entidades/
                │   │   │   ├── Transaccion.kt
                │   │   │   ├── Categoria.kt
                │   │   │   ├── Cuenta.kt
                │   │   │   └── Presupuesto.kt
                │   │   ├── dao/
                │   │   │   ├── TransaccionDao.kt
                │   │   │   ├── CategoriaDao.kt
                │   │   │   ├── CuentaDao.kt
                │   │   │   └── PresupuestoDao.kt
                │   │   └── base/
                │   │       └── BaseDatosFinanzas.kt
                │   ├── remoto/
                │   │   ├── api/
                │   │   │   └── ApiDivisas.kt
                │   │   └── dto/
                │   │       └── TipoCambioDto.kt
                │   └── repositorios/
                │       ├── RepositorioFinanzas.kt
                │       └── RepositorioDivisas.kt
                │
                ├── ui/
                │   ├── pantallas/
                │   │   ├── login/
                │   │   │   ├── PantallaLogin.kt
                │   │   │   └── LoginViewModel.kt
                │   │   ├── inicio/
                │   │   │   ├── PantallaInicio.kt
                │   │   │   └── InicioViewModel.kt
                │   │   ├── detalle/
                │   │   │   ├── PantallaDetalle.kt
                │   │   │   └── DetalleViewModel.kt
                │   │   └── formulario/
                │   │       ├── PantallaFormulario.kt
                │   │       └── FormularioViewModel.kt
                │   ├── componentes/
                │   │   ├── BotonPrincipal.kt
                │   │   ├── TarjetaTransaccion.kt
                │   │   └── GraficoCircular.kt
                │   ├── navegacion/
                │   │   └── NavegacionApp.kt
                │   └── tema/
                │       ├── Color.kt
                │       ├── Tipo.kt
                │       └── Tema.kt
                │
                └── utilidades/
                    ├── GestorAuth.kt
                    └── Constantes.kt
```

## Archivos Clave y su Propósito

### Configuración del Proyecto

#### `build.gradle.kts` (raíz y app)
- Configuración de compilación
- Dependencias de Jetpack Compose, Room, Retrofit, Auth0
- Versiones de SDK y compilador

### Capa de Datos

#### Entidades Room
- `Transaccion.kt`: Modelo de datos para gastos/ingresos
- `Categoria.kt`: Categorías para clasificar transacciones
- `Cuenta.kt`: Cuentas bancarias o de efectivo
- `Presupuesto.kt`: Límites mensuales por categoría

#### DAOs
- Interfaces con operaciones CRUD para cada entidad
- Uso de @Query para consultas personalizadas
- Retorno de Flow para reactividad

#### Base de Datos
- `BaseDatosFinanzas.kt`: Clase principal de Room
- Inicialización con datos por defecto
- Patrón Singleton para única instancia

#### API y DTOs
- `ApiDivisas.kt`: Interfaz Retrofit para tipos de cambio
- `TipoCambioDto.kt`: Modelos para respuestas JSON

#### Repositorios
- Abstracción entre fuentes de datos y ViewModels
- Lógica de negocio (cálculos, filtrados)
- Manejo de errores

### Capa de Presentación (UI)

#### ViewModels
- Gestión del estado de cada pantalla
- Sealed classes para estados (Cargando, Éxito, Error)
- Exposición de datos con StateFlow

#### Pantallas Compose
- `PantallaLogin.kt`: Autenticación con Auth0
- `PantallaInicio.kt`: Dashboard con resumen y lista
- `PantallaDetalle.kt`: Vista detallada de transacción
- `PantallaFormulario.kt`: Crear/editar transacciones

#### Componentes Reutilizables
- `BotonPrincipal.kt`: Botón estilizado
- `TarjetaTransaccion.kt`: Card para listar transacciones
- `GraficoCircular.kt`: Visualización de gastos

#### Navegación
- `NavegacionApp.kt`: NavHost con todas las rutas
- Gestión de argumentos de navegación
- ViewModelFactory personalizado

#### Tema
- `Color.kt`: Paleta de colores
- `Tipo.kt`: Tipografía
- `Tema.kt`: Theme de Material Design 3

### Utilidades

#### `GestorAuth.kt`
- Wrapper para Auth0
- Métodos de login/logout
- Gestión de credenciales

#### `Constantes.kt`
- URLs de APIs
- Rutas de navegación
- Valores globales

### Actividad Principal

#### `MainActivity.kt`
- Punto de entrada de la app
- Inicialización de base de datos
- Setup de Retrofit
- Seteo del tema Compose

## Flujo de Datos

```
Usuario interactúa con UI (Compose)
    ↓
Composable notifica al ViewModel
    ↓
ViewModel procesa con Repositorio
    ↓
Repositorio accede a DAO (Room) o API (Retrofit)
    ↓
Datos retornan via Flow/suspend
    ↓
ViewModel actualiza StateFlow
    ↓
Composable se recompone automáticamente
```

## Estados de la Aplicación

### Pantalla de Login
- Inicial: Esperando acción del usuario
- Cargando: Procesando login con Auth0
- Autenticado: Login exitoso
- Error: Mensaje de error

### Pantalla de Inicio
- Cargando: Obteniendo datos de BD
- Éxito: Datos cargados correctamente
- Error: Problema al cargar

### Pantalla de Detalle
- Cargando: Obteniendo transacción
- Éxito: Transacción cargada
- Error: Transacción no encontrada
- Borrado: Transacción eliminada

### Pantalla de Formulario
- Inicial/Cargando: Preparando formulario
- CargandoDatos: Datos listos, formulario interactivo
- Error: Validación fallida
- Guardado: Transacción guardada

## Principales Decisiones Técnicas

### 1. ViewModels sin Hilt
Se optó por factories manuales en lugar de Hilt para mantener el código más simple y comprensible para un estudiante. Aunque es más verboso, es más transparente.

### 2. Un Repositorio para Finanzas
En lugar de un repositorio por entidad, se agrupó todo en `RepositorioFinanzas`. Esto reduce la complejidad sin perder funcionalidad.

### 3. Datos Iniciales en la BD
La base de datos se puebla automáticamente con categorías, cuentas y presupuestos al crearse. Mejora UX permitiendo probar la app sin configuración previa.

### 4. Gráfico Custom con Canvas
Se implementó un gráfico circular propio en lugar de usar librerías de terceros. Demuestra conocimiento de Compose y reduce dependencias.

### 5. Manejo de Errores Simple
Try-catch básicos con mensajes descriptivos en lugar de sistemas complejos de manejo de errores. Apropiado para el nivel de un estudiante de DAM.

### 6. API Pública sin Key
Se eligió una API de divisas que no requiere autenticación para simplificar la configuración. La funcionalidad es opcional y no bloquea la app si falla.

## Configuración Adicional Necesaria

### AndroidManifest.xml
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <uses-permission android:name="android.permission.INTERNET" />
    
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="ExpenseTracker"
        android:supportsRtl="true"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.AppCompat.Light.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            
            <!-- Auth0 callback -->
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data
                    android:host="tu-dominio.auth0.com"
                    android:pathPrefix="/android/com.dam.expensetracker/callback"
                    android:scheme="com.dam.expensetracker" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### settings.gradle.kts
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ExpenseTracker"
include(":app")
```

## Notas para el Alumno

1. **Imports**: Asegúrate de que Android Studio importe automáticamente todas las clases. Si hay errores de import, usa Alt+Enter para resolverlos.

2. **Build Variants**: Usa "debug" durante el desarrollo. "release" requiere configuración de firma.

3. **Emulador**: Usa un emulador con API 24 o superior. Recomendado: Pixel 5 con API 33.

4. **Logs**: Usa `Log.d("TAG", "mensaje")` para debuggear. Los logs aparecen en el Logcat de Android Studio.

5. **Cambios en la BD**: Si cambias las entidades de Room, incrementa la versión en `BaseDatosFinanzas` o desinstala la app para recrear la BD.

6. **Pruebas**: Prueba todas las funcionalidades en el emulador antes de presentar. Comprueba que el flujo completo funcione.

## Comandos Útiles de Gradle

```bash
# Limpiar el proyecto
./gradlew clean

# Compilar el proyecto
./gradlew build

# Compilar y ejecutar en dispositivo conectado
./gradlew installDebug

# Ver dependencias
./gradlew app:dependencies
```

## Recursos de Ayuda

- Documentación oficial de Jetpack Compose: https://developer.android.com/jetpack/compose
- Guía de Room: https://developer.android.com/training/data-storage/room
- Tutorial de Retrofit: https://square.github.io/retrofit/
- Documentación de Auth0 Android: https://auth0.com/docs/quickstart/native/android

## Contacto y Soporte

Para dudas sobre el proyecto, consultar con el profesor del módulo o revisar la documentación oficial de Android.
