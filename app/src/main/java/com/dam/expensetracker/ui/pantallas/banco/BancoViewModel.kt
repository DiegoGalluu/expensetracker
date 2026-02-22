package com.dam.expensetracker.ui.pantallas.banco

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.expensetracker.datos.remoto.dto.MovimientoBancarioUi
import com.dam.expensetracker.datos.repositorios.RepositorioBancarioSimulado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EstadoBanco {
    object Cargando : EstadoBanco()
    data class Exito(
        val saldoDisponible: Double,
        val gastosUltimosMovimientos: Double,
        val movimientos: List<MovimientoBancarioUi>
    ) : EstadoBanco()
    data class Error(val mensaje: String) : EstadoBanco()
}

class BancoViewModel(
    private val repositorioBancarioSimulado: RepositorioBancarioSimulado
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoBanco>(EstadoBanco.Cargando)
    val estado: StateFlow<EstadoBanco> = _estado.asStateFlow()

    init {
        cargarResumenBancario()
    }

    fun cargarResumenBancario() {
        viewModelScope.launch {
            _estado.value = EstadoBanco.Cargando

            val resumen = repositorioBancarioSimulado.obtenerResumenBancario()
            if (resumen == null) {
                _estado.value = EstadoBanco.Error("No se pudo cargar la API bancaria simulada")
                return@launch
            }

            _estado.value = EstadoBanco.Exito(
                saldoDisponible = resumen.saldoDisponible,
                gastosUltimosMovimientos = resumen.gastosUltimosMovimientos,
                movimientos = resumen.movimientos
            )
        }
    }
}
