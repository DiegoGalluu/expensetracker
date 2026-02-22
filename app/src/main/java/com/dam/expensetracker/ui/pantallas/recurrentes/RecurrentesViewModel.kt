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
import kotlinx.coroutines.launch

sealed class EstadoRecurrentes {
    object Cargando : EstadoRecurrentes()
    data class Exito(
        val recurrentes: List<RecurringTransaction>,
        val categorias: List<Categoria>,
        val cuentas: List<Cuenta>
    ) : EstadoRecurrentes()
    data class Error(val mensaje: String) : EstadoRecurrentes()
}

class RecurrentesViewModel(
    private val repositorio: RepositorioFinanzas
) : ViewModel() {

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
                    EstadoRecurrentes.Exito(
                        recurrentes = recurrentes,
                        categorias = categorias,
                        cuentas = cuentas
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
}
