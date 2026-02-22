package com.dam.expensetracker.ui.pantallas.cuentas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.expensetracker.datos.local.entidades.Cuenta
import com.dam.expensetracker.datos.repositorios.RepositorioFinanzas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class EstadoCuentas {
    object Cargando : EstadoCuentas()
    data class Exito(val cuentas: List<Cuenta>) : EstadoCuentas()
    data class Error(val mensaje: String) : EstadoCuentas()
}

class CuentasViewModel(
    private val repositorio: RepositorioFinanzas
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoCuentas>(EstadoCuentas.Cargando)
    val estado: StateFlow<EstadoCuentas> = _estado.asStateFlow()

    init {
        cargarCuentas()
    }

    fun cargarCuentas() {
        viewModelScope.launch {
            try {
                asegurarCuentaEfectivo()

                combine(
                    repositorio.obtenerTodasCuentas()
                ) { cuentas ->
                    EstadoCuentas.Exito(cuentas = cuentas.first())
                }.collect { estadoNuevo ->
                    _estado.value = estadoNuevo
                }
            } catch (e: Exception) {
                _estado.value = EstadoCuentas.Error("Error al cargar cuentas: ${e.message}")
            }
        }
    }

    fun crearCuenta(nombre: String) {
        viewModelScope.launch {
            val nombreNormalizado = nombre.trim()

            if (nombreNormalizado.isBlank()) {
                _estado.value = EstadoCuentas.Error("El nombre de la cuenta es obligatorio")
                return@launch
            }

            try {
                val cuentas = repositorio.obtenerTodasCuentas().first()
                val duplicada = cuentas.any { it.nombre.equals(nombreNormalizado, ignoreCase = true) }

                if (duplicada) {
                    _estado.value = EstadoCuentas.Error("Ya existe una cuenta con ese nombre")
                    return@launch
                }

                repositorio.insertarCuenta(Cuenta(nombre = nombreNormalizado))
            } catch (e: Exception) {
                _estado.value = EstadoCuentas.Error("Error al crear cuenta: ${e.message}")
            }
        }
    }

    fun borrarCuenta(idCuenta: Long) {
        viewModelScope.launch {
            try {
                val cuenta = repositorio.obtenerCuentaPorId(idCuenta)
                if (cuenta == null) return@launch

                if (cuenta.nombre.equals("Efectivo", ignoreCase = true)) {
                    _estado.value = EstadoCuentas.Error("La cuenta Efectivo es fija y no se puede borrar")
                    return@launch
                }

                val transaccionesCuenta = repositorio.obtenerTransaccionesPorCuenta(idCuenta).first()
                if (transaccionesCuenta.isNotEmpty()) {
                    _estado.value = EstadoCuentas.Error("No puedes borrar esta cuenta porque tiene transacciones asociadas")
                    return@launch
                }

                repositorio.borrarCuenta(cuenta)
            } catch (e: Exception) {
                _estado.value = EstadoCuentas.Error("Error al borrar cuenta: ${e.message}")
            }
        }
    }

    private suspend fun asegurarCuentaEfectivo() {
        val cuentas = repositorio.obtenerTodasCuentas().first()
        val existeEfectivo = cuentas.any { it.nombre.equals("Efectivo", ignoreCase = true) }

        if (!existeEfectivo) {
            repositorio.insertarCuenta(Cuenta(nombre = "Efectivo"))
        }
    }
}
