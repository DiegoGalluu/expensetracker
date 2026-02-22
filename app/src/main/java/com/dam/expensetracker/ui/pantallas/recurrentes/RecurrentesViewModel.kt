package com.dam.expensetracker.ui.pantallas.recurrentes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.expensetracker.datos.local.entidades.Categoria
import com.dam.expensetracker.datos.local.entidades.Cuenta
import com.dam.expensetracker.datos.local.entidades.RecurringTransaction
import com.dam.expensetracker.datos.repositorios.RepositorioFinanzas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class EstadoRecurrentes {
    object Cargando : EstadoRecurrentes()
    data class Exito(
        val recurrentes: List<RecurringTransaction>,
        val categorias: List<Categoria>,
        val cuentas: List<Cuenta>,
        val ingresosFijosMensuales: Double,
        val gastosFijosMensuales: Double
    ) : EstadoRecurrentes()
    data class Error(val mensaje: String) : EstadoRecurrentes()
}

class RecurrentesViewModel(
    private val repositorio: RepositorioFinanzas
) : ViewModel() {

    private val categoriasPredeterminadasLegacy = setOf(
        "Comida", "Transporte", "Ocio", "Salud", "Vivienda", "Educación", "Otros"
    )

    private val _estado = MutableStateFlow<EstadoRecurrentes>(EstadoRecurrentes.Cargando)
    val estado: StateFlow<EstadoRecurrentes> = _estado.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            try {
                combine(
                    repositorio.obtenerTodasTransaccionesRecurrentes(),
                    repositorio.obtenerTodasCategorias(),
                    repositorio.obtenerTodasCuentas()
                ) { recurrentes, categorias, cuentas ->
                    val categoriasFiltradas = categorias.filterNot { categoria ->
                        categoriasPredeterminadasLegacy.any {
                            it.equals(categoria.nombre.trim(), ignoreCase = true)
                        }
                    }

                    val recurrentesActivas = recurrentes.filter { it.activo }
                    val ingresosFijosMensuales = recurrentesActivas
                        .filter { !it.esGasto }
                        .sumOf { recurrente -> convertirAMensual(recurrente.cantidad, recurrente.frecuencia) }

                    val gastosFijosMensuales = recurrentesActivas
                        .filter { it.esGasto }
                        .sumOf { recurrente -> convertirAMensual(recurrente.cantidad, recurrente.frecuencia) }

                    EstadoRecurrentes.Exito(
                        recurrentes = recurrentes,
                        categorias = categoriasFiltradas,
                        cuentas = cuentas,
                        ingresosFijosMensuales = ingresosFijosMensuales,
                        gastosFijosMensuales = gastosFijosMensuales
                    )
                }.collect { nuevoEstado ->
                    _estado.value = nuevoEstado
                }
            } catch (e: Exception) {
                _estado.value = EstadoRecurrentes.Error("Error al cargar recurrentes: ${e.message}")
            }
        }
    }

    fun crearRecurrente(
        nombre: String,
        cantidadTexto: String,
        esGasto: Boolean,
        idCategoria: Long?,
        idCuenta: Long?,
        frecuencia: String,
        nota: String
    ) {
        viewModelScope.launch {
            try {
                val nombreNormalizado = nombre.trim()
                val cantidad = cantidadTexto.trim().replace(',', '.').toDoubleOrNull()

                if (nombreNormalizado.isBlank()) {
                    _estado.value = EstadoRecurrentes.Error("El nombre es obligatorio")
                    return@launch
                }

                if (cantidad == null || cantidad <= 0) {
                    _estado.value = EstadoRecurrentes.Error("La cantidad debe ser mayor a 0")
                    return@launch
                }

                if (idCategoria == null || idCuenta == null) {
                    _estado.value = EstadoRecurrentes.Error("Debes seleccionar categoría y cuenta")
                    return@launch
                }

                repositorio.insertarTransaccionRecurrente(
                    RecurringTransaction(
                        nombre = nombreNormalizado,
                        cantidad = cantidad,
                        esGasto = esGasto,
                        idCategoria = idCategoria,
                        idCuenta = idCuenta,
                        frecuencia = frecuencia,
                        fechaProximaEjecucion = System.currentTimeMillis(),
                        activo = true,
                        nota = nota.trim()
                    )
                )
            } catch (e: Exception) {
                _estado.value = EstadoRecurrentes.Error("Error al crear recurrente: ${e.message}")
            }
        }
    }

    fun borrarRecurrente(idRecurrente: Long) {
        viewModelScope.launch {
            try {
                val estadoActual = _estado.value
                if (estadoActual !is EstadoRecurrentes.Exito) return@launch

                val recurrente = estadoActual.recurrentes.find { it.id == idRecurrente } ?: return@launch
                repositorio.borrarTransaccionRecurrente(recurrente)
            } catch (e: Exception) {
                _estado.value = EstadoRecurrentes.Error("Error al borrar recurrente: ${e.message}")
            }
        }
    }

    fun actualizarRecurrente(
        idRecurrente: Long,
        nombre: String,
        cantidadTexto: String,
        esGasto: Boolean,
        activo: Boolean,
        idCategoria: Long?,
        idCuenta: Long?,
        frecuencia: String,
        nota: String
    ) {
        viewModelScope.launch {
            try {
                val estadoActual = _estado.value
                if (estadoActual !is EstadoRecurrentes.Exito) return@launch

                val recurrenteExistente = estadoActual.recurrentes.find { it.id == idRecurrente }
                if (recurrenteExistente == null) {
                    _estado.value = EstadoRecurrentes.Error("No se encontró la recurrente a editar")
                    return@launch
                }

                val nombreNormalizado = nombre.trim()
                val cantidad = cantidadTexto.trim().replace(',', '.').toDoubleOrNull()

                if (nombreNormalizado.isBlank()) {
                    _estado.value = EstadoRecurrentes.Error("El nombre es obligatorio")
                    return@launch
                }

                if (cantidad == null || cantidad <= 0) {
                    _estado.value = EstadoRecurrentes.Error("La cantidad debe ser mayor a 0")
                    return@launch
                }

                if (idCategoria == null || idCuenta == null) {
                    _estado.value = EstadoRecurrentes.Error("Debes seleccionar categoría y cuenta")
                    return@launch
                }

                repositorio.actualizarTransaccionRecurrente(
                    recurrenteExistente.copy(
                        nombre = nombreNormalizado,
                        cantidad = cantidad,
                        esGasto = esGasto,
                        activo = activo,
                        idCategoria = idCategoria,
                        idCuenta = idCuenta,
                        frecuencia = frecuencia,
                        nota = nota.trim()
                    )
                )
            } catch (e: Exception) {
                _estado.value = EstadoRecurrentes.Error("Error al actualizar recurrente: ${e.message}")
            }
        }
    }

    fun crearCategoriaDesdeSelector(nombreCategoria: String, onResultado: (String?) -> Unit) {
        viewModelScope.launch {
            val nombreNormalizado = nombreCategoria.trim()

            if (nombreNormalizado.isBlank()) {
                onResultado("El nombre de la categoría es obligatorio")
                return@launch
            }

            if (nombreNormalizado.equals("Ahorro mensual", ignoreCase = true)) {
                onResultado("Ahorro mensual ya existe como categoría fija")
                return@launch
            }

            if (categoriasPredeterminadasLegacy.any { it.equals(nombreNormalizado, ignoreCase = true) }) {
                onResultado("Esa categoría legacy está bloqueada")
                return@launch
            }

            try {
                val categoriasExistentes = repositorio.obtenerTodasCategorias().first()
                val duplicada = categoriasExistentes.any {
                    it.nombre.trim().equals(nombreNormalizado, ignoreCase = true)
                }

                if (duplicada) {
                    onResultado("Ya existe una categoría con ese nombre")
                    return@launch
                }

                repositorio.insertarCategoria(
                    Categoria(
                        nombre = nombreNormalizado,
                        color = "#1976D2"
                    )
                )

                onResultado(null)
            } catch (e: Exception) {
                onResultado("No se pudo crear la categoría: ${e.message}")
            }
        }
    }

    private fun convertirAMensual(cantidad: Double, frecuencia: String): Double {
        return when (frecuencia) {
            "DIARIA" -> cantidad * 30.0
            "SEMANAL" -> cantidad * 4.0
            else -> cantidad
        }
    }
}
