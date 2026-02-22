package com.dam.expensetracker.ui.pantallas.divisas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.expensetracker.datos.repositorios.RepositorioDivisas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EstadoDivisas {
    object Inicial : EstadoDivisas()
    object Cargando : EstadoDivisas()
    data class Exito(
        val monedaOrigen: String,
        val monedaDestino: String,
        val cantidad: Double,
        val resultado: Double,
        val tasa: Double,
        val fechaActualizacion: String?
    ) : EstadoDivisas()
    data class Error(val mensaje: String) : EstadoDivisas()
}

class DivisasViewModel(
    private val repositorioDivisas: RepositorioDivisas
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoDivisas>(EstadoDivisas.Inicial)
    val estado: StateFlow<EstadoDivisas> = _estado.asStateFlow()

    val monedasDisponibles = listOf("EUR", "USD", "GBP", "JPY", "CHF")

    fun convertir(monedaOrigen: String, monedaDestino: String, cantidadTexto: String) {
        viewModelScope.launch {
            val cantidad = cantidadTexto.trim().replace(',', '.').toDoubleOrNull()

            if (cantidad == null || cantidad <= 0) {
                _estado.value = EstadoDivisas.Error("Introduce una cantidad válida mayor que 0")
                return@launch
            }

            if (monedaOrigen == monedaDestino) {
                _estado.value = EstadoDivisas.Error("Selecciona monedas diferentes")
                return@launch
            }

            _estado.value = EstadoDivisas.Cargando

            try {
                val tipos = repositorioDivisas.obtenerTiposCambio(monedaOrigen)
                val tasa = tipos?.tasas?.get(monedaDestino)

                if (tasa == null) {
                    _estado.value = EstadoDivisas.Error("No se pudo obtener la tasa actual para esa conversión")
                    return@launch
                }

                _estado.value = EstadoDivisas.Exito(
                    monedaOrigen = monedaOrigen,
                    monedaDestino = monedaDestino,
                    cantidad = cantidad,
                    resultado = cantidad * tasa,
                    tasa = tasa,
                    fechaActualizacion = tipos.fecha
                )
            } catch (e: Exception) {
                _estado.value = EstadoDivisas.Error("No se pudo conectar con la API de divisas")
            }
        }
    }
}
