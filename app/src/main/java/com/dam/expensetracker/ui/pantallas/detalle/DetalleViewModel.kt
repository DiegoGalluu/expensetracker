package com.dam.expensetracker.ui.pantallas.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.expensetracker.datos.local.entidades.Categoria
import com.dam.expensetracker.datos.local.entidades.Cuenta
import com.dam.expensetracker.datos.local.entidades.Transaccion
import com.dam.expensetracker.datos.repositorios.RepositorioFinanzas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados posibles de la pantalla de detalle
 */
sealed class EstadoDetalle {
    object Cargando : EstadoDetalle()
    data class Exito(
        val transaccion: Transaccion,
        val categoria: Categoria?,
        val cuenta: Cuenta?
    ) : EstadoDetalle()
    data class Error(val mensaje: String) : EstadoDetalle()
    object Borrado : EstadoDetalle()
}

/**
 * ViewModel para la pantalla de detalle de una transacción
 */
class DetalleViewModel(
    private val repositorio: RepositorioFinanzas
) : ViewModel() {
    
    private val _estado = MutableStateFlow<EstadoDetalle>(EstadoDetalle.Cargando)
    val estado: StateFlow<EstadoDetalle> = _estado.asStateFlow()
    
    /**
     * Carga los datos de una transacción específica
     */
    fun cargarTransaccion(transaccionId: Long) {
        viewModelScope.launch {
            try {
                _estado.value = EstadoDetalle.Cargando
                
                // Obtener la transacción
                val transaccion = repositorio.obtenerTransaccionPorId(transaccionId)
                
                if (transaccion != null) {
                    // Obtener categoría y cuenta relacionadas
                    val categoria = repositorio.obtenerCategoriaPorId(transaccion.idCategoria)
                    val cuenta = repositorio.obtenerCuentaPorId(transaccion.idCuenta)
                    
                    _estado.value = EstadoDetalle.Exito(
                        transaccion = transaccion,
                        categoria = categoria,
                        cuenta = cuenta
                    )
                } else {
                    _estado.value = EstadoDetalle.Error("Transacción no encontrada")
                }
                
            } catch (e: Exception) {
                _estado.value = EstadoDetalle.Error("Error al cargar: ${e.message}")
            }
        }
    }
    
    /**
     * Borra la transacción actual
     */
    fun borrarTransaccion(transaccion: Transaccion) {
        viewModelScope.launch {
            try {
                repositorio.borrarTransaccion(transaccion)
                _estado.value = EstadoDetalle.Borrado
            } catch (e: Exception) {
                _estado.value = EstadoDetalle.Error("Error al borrar: ${e.message}")
            }
        }
    }
}
