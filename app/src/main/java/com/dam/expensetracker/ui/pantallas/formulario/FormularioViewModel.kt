package com.dam.expensetracker.ui.pantallas.formulario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.expensetracker.datos.local.entidades.Categoria
import com.dam.expensetracker.datos.local.entidades.Cuenta
import com.dam.expensetracker.datos.local.entidades.Transaccion
import com.dam.expensetracker.datos.repositorios.RepositorioFinanzas
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Estados del formulario
 */
sealed class EstadoFormulario {
    object Inicial : EstadoFormulario()
    object Cargando : EstadoFormulario()
    data class CargandoDatos(
        val categorias: List<Categoria>,
        val cuentas: List<Cuenta>,
        val transaccionExistente: Transaccion?
    ) : EstadoFormulario()
    data class CargandoDatosConError(
        val categorias: List<Categoria>,
        val cuentas: List<Cuenta>,
        val transaccionExistente: Transaccion?,
        val mensaje: String
    ) : EstadoFormulario()
    object Guardado : EstadoFormulario()
    data class Error(val mensaje: String) : EstadoFormulario()
}

/**
 * ViewModel para el formulario de crear/editar transacción
 */
class FormularioViewModel(
    private val repositorio: RepositorioFinanzas
) : ViewModel() {

    private val categoriasPredeterminadasLegacy = setOf(
        "Comida", "Transporte", "Ocio", "Salud", "Vivienda", "Educación", "Otros"
    )
    
    private val _estado = MutableStateFlow<EstadoFormulario>(EstadoFormulario.Inicial)
    val estado: StateFlow<EstadoFormulario> = _estado.asStateFlow()
    
    // Campos del formulario
    private val _cantidad = MutableStateFlow("")
    val cantidad: StateFlow<String> = _cantidad.asStateFlow()
    
    private val _nota = MutableStateFlow("")
    val nota: StateFlow<String> = _nota.asStateFlow()
    
    private val _esGasto = MutableStateFlow(true)
    val esGasto: StateFlow<Boolean> = _esGasto.asStateFlow()
    
    private val _categoriaSeleccionada = MutableStateFlow<Categoria?>(null)
    val categoriaSeleccionada: StateFlow<Categoria?> = _categoriaSeleccionada.asStateFlow()
    
    private val _cuentaSeleccionada = MutableStateFlow<Cuenta?>(null)
    val cuentaSeleccionada: StateFlow<Cuenta?> = _cuentaSeleccionada.asStateFlow()
    
    private var transaccionId: Long? = null
    
    /**
     * Carga los datos necesarios para el formulario
     * Si transaccionId no es null, carga los datos de esa transacción para editarla
     */
    fun cargarDatos(transaccionId: Long? = null) {
        this.transaccionId = transaccionId
        
        viewModelScope.launch {
            try {
                _estado.value = EstadoFormulario.Cargando

                asegurarCategoriaAhorroMensual()
                asegurarCuentaEfectivo()
                
                // Combinar flows de categorías y cuentas
                combine(
                    repositorio.obtenerTodasCategorias(),
                    repositorio.obtenerTodasCuentas()
                ) { categorias, cuentas ->
                    val categoriasFiltradas = categorias.filterNot {
                        it.id <= 7 && it.nombre in categoriasPredeterminadasLegacy
                    }
                    Pair(categoriasFiltradas, cuentas)
                }.first().let { (categorias, cuentas) ->
                    
                    // Si hay un ID, cargar la transacción existente
                    val transaccionExistente = if (transaccionId != null) {
                        val trans = repositorio.obtenerTransaccionPorId(transaccionId)
                        
                        // Rellenar los campos con los datos existentes
                        trans?.let {
                            _cantidad.value = it.cantidad.toString()
                            _nota.value = it.nota
                            _esGasto.value = it.esGasto
                            _categoriaSeleccionada.value = categorias.find { cat -> cat.id == it.idCategoria }
                            _cuentaSeleccionada.value = cuentas.find { cta -> cta.id == it.idCuenta }
                        }
                        
                        trans
                    } else {
                        // Modo crear: categoría sin seleccionar para forzar selección explícita
                        _categoriaSeleccionada.value = null
                        _cuentaSeleccionada.value = cuentas.firstOrNull()
                        null
                    }
                    
                    _estado.value = EstadoFormulario.CargandoDatos(
                        categorias = categorias,
                        cuentas = cuentas,
                        transaccionExistente = transaccionExistente
                    )
                }
                
            } catch (e: Exception) {
                _estado.value = EstadoFormulario.Error("Error al cargar datos: ${e.message}")
            }
        }
    }

    private suspend fun asegurarCategoriaAhorroMensual() {
        val categorias = repositorio.obtenerTodasCategorias().first()
        val existeAhorroMensual = categorias.any { it.nombre.equals("Ahorro mensual", ignoreCase = true) }

        if (!existeAhorroMensual) {
            repositorio.insertarCategoria(
                Categoria(
                    nombre = "Ahorro mensual",
                    color = "#2E7D32"
                )
            )
        }
    }

    private suspend fun asegurarCuentaEfectivo() {
        val cuentas = repositorio.obtenerTodasCuentas().first()
        val existeEfectivo = cuentas.any { it.nombre.equals("Efectivo", ignoreCase = true) }

        if (!existeEfectivo) {
            repositorio.insertarCuenta(
                Cuenta(nombre = "Efectivo")
            )
        }
    }
    
    /**
     * Actualiza la cantidad ingresada
     */
    fun actualizarCantidad(nuevaCantidad: String) {
        _cantidad.value = nuevaCantidad
    }
    
    /**
     * Actualiza la nota
     */
    fun actualizarNota(nuevaNota: String) {
        _nota.value = nuevaNota
    }
    
    /**
     * Cambia entre gasto e ingreso
     */
    fun cambiarTipo(esGasto: Boolean) {
        _esGasto.value = esGasto
    }
    
    /**
     * Selecciona una categoría
     */
    fun seleccionarCategoria(categoria: Categoria) {
        _categoriaSeleccionada.value = categoria
    }
    
    /**
     * Selecciona una cuenta
     */
    fun seleccionarCuenta(cuenta: Cuenta) {
        _cuentaSeleccionada.value = cuenta
    }
    
    /**
     * Valida y guarda la transacción
     */
    fun guardarTransaccion() {
        viewModelScope.launch {
            try {
                // Validaciones
                val cantidadDouble = _cantidad.value
                    .trim()
                    .replace(',', '.')
                    .toDoubleOrNull()
                if (cantidadDouble == null || cantidadDouble <= 0) {
                    establecerErrorFormulario("La cantidad debe ser mayor a 0")
                    return@launch
                }
                
                if (_categoriaSeleccionada.value == null) {
                    establecerErrorFormulario("Debes seleccionar una categoría")
                    return@launch
                }
                
                if (_cuentaSeleccionada.value == null) {
                    establecerErrorFormulario("Debes seleccionar una cuenta")
                    return@launch
                }
                
                // Crear o actualizar transacción
                val transaccion = Transaccion(
                    id = transaccionId ?: 0,
                    cantidad = cantidadDouble,
                    fecha = System.currentTimeMillis(),
                    esGasto = _esGasto.value,
                    idCategoria = _categoriaSeleccionada.value!!.id,
                    idCuenta = _cuentaSeleccionada.value!!.id,
                    nota = _nota.value
                )
                
                if (transaccionId == null) {
                    // Crear nueva
                    repositorio.insertarTransaccion(transaccion)
                } else {
                    // Actualizar existente
                    repositorio.actualizarTransaccion(transaccion)
                }
                
                _estado.value = EstadoFormulario.Guardado
                
            } catch (e: Exception) {
                establecerErrorFormulario("Error al guardar: ${e.message}")
            }
        }
    }

    private fun establecerErrorFormulario(mensaje: String) {
        val estadoActual = _estado.value
        if (estadoActual is EstadoFormulario.CargandoDatos) {
            _estado.value = EstadoFormulario.CargandoDatosConError(
                categorias = estadoActual.categorias,
                cuentas = estadoActual.cuentas,
                transaccionExistente = estadoActual.transaccionExistente,
                mensaje = mensaje
            )
            return
        }

        if (estadoActual is EstadoFormulario.CargandoDatosConError) {
            _estado.value = estadoActual.copy(mensaje = mensaje)
            return
        }

        _estado.value = EstadoFormulario.Error(mensaje)
    }
    
    /**
     * Resetea el mensaje de error
     */
    fun limpiarError() {
        val estadoActual = _estado.value
        if (estadoActual is EstadoFormulario.Error) {
            // Deberíamos volver al estado anterior o a un estado que permita seguir editando
            // Para simplificar, si hay un error y limpiamos, intentamos cargar de nuevo o volver a CargandoDatos
        }
    }
}
