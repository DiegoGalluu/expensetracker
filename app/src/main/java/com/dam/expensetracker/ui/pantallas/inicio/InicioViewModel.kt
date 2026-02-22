package com.dam.expensetracker.ui.pantallas.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.expensetracker.datos.local.entidades.Categoria
import com.dam.expensetracker.datos.local.entidades.RecurringTransaction
import com.dam.expensetracker.datos.local.entidades.Transaccion
import com.dam.expensetracker.datos.remoto.dto.TipoCambioDto
import com.dam.expensetracker.datos.repositorios.RepositorioDivisas
import com.dam.expensetracker.datos.repositorios.RepositorioFinanzas
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Estados posibles de la pantalla de inicio
 */
sealed class EstadoInicio {
    object Cargando : EstadoInicio()
    data class Exito(
        val transacciones: List<Transaccion>,
        val categorias: List<Categoria>,
        val saldoTotal: Double,
        val totalGastosMes: Double,
        val tipoCambio: TipoCambioDto?
    ) : EstadoInicio()
    data class Error(val mensaje: String) : EstadoInicio()
}

/**
 * ViewModel para la pantalla de inicio/dashboard
 */
class InicioViewModel(
    private val repositorioFinanzas: RepositorioFinanzas,
    private val repositorioDivisas: RepositorioDivisas
) : ViewModel() {
    
    private val _estado = MutableStateFlow<EstadoInicio>(EstadoInicio.Cargando)
    val estado: StateFlow<EstadoInicio> = _estado.asStateFlow()
    
    init {
        cargarDatos()
    }
    
    /**
     * Carga todos los datos necesarios para la pantalla de inicio
     */
    fun cargarDatos() {
        viewModelScope.launch {
            try {
                _estado.value = EstadoInicio.Cargando

                procesarRecurrentesPendientes()
                
                // Combinar los flows de transacciones y categorías
                combine(
                    repositorioFinanzas.obtenerTodasTransacciones(),
                    repositorioFinanzas.obtenerTodasCategorias()
                ) { transacciones, categorias ->
                    Pair(transacciones, categorias)
                }.collect { (transacciones, categorias) ->
                    
                    // Calcular saldo total (ingresos - gastos)
                    val saldoTotal = calcularSaldoTotal(transacciones)
                    
                    // Calcular total de gastos del mes
                    val totalGastosMes = calcularGastosMes(transacciones)
                    
                    // Obtener tipos de cambio (opcional, puede fallar)
                    val tipoCambio = try {
                        repositorioDivisas.obtenerTiposCambio("EUR")
                    } catch (e: Exception) {
                        null
                    }
                    
                    _estado.value = EstadoInicio.Exito(
                        transacciones = transacciones,
                        categorias = categorias,
                        saldoTotal = saldoTotal,
                        totalGastosMes = totalGastosMes,
                        tipoCambio = tipoCambio
                    )
                }
                
            } catch (e: Exception) {
                _estado.value = EstadoInicio.Error("Error al cargar datos: ${e.message}")
            }
        }
    }

    private suspend fun procesarRecurrentesPendientes() {
        val ahora = System.currentTimeMillis()
        val recurrentesVencidas = repositorioFinanzas.obtenerRecurrentesActivasVencidas(ahora)

        recurrentesVencidas.forEach { recurrente ->
            val transaccion = Transaccion(
                cantidad = recurrente.cantidad,
                fecha = ahora,
                esGasto = recurrente.esGasto,
                idCategoria = recurrente.idCategoria,
                idCuenta = recurrente.idCuenta,
                nota = if (recurrente.nota.isNotBlank()) {
                    "${recurrente.nota} (recurrente)"
                } else {
                    "${recurrente.nombre} (recurrente)"
                }
            )

            repositorioFinanzas.insertarTransaccion(transaccion)

            val siguienteEjecucion = calcularSiguienteEjecucion(recurrente, ahora)
            repositorioFinanzas.actualizarTransaccionRecurrente(
                recurrente.copy(
                    ultimaEjecucion = ahora,
                    fechaProximaEjecucion = siguienteEjecucion
                )
            )
        }
    }

    private fun calcularSiguienteEjecucion(recurrente: RecurringTransaction, referencia: Long): Long {
        val calendario = java.util.Calendar.getInstance()
        calendario.timeInMillis = maxOf(recurrente.fechaProximaEjecucion, referencia)

        when (recurrente.frecuencia) {
            "DIARIA" -> calendario.add(java.util.Calendar.DAY_OF_YEAR, 1)
            "SEMANAL" -> calendario.add(java.util.Calendar.WEEK_OF_YEAR, 1)
            else -> calendario.add(java.util.Calendar.MONTH, 1)
        }

        return calendario.timeInMillis
    }
    
    /**
     * Calcula el saldo total (ingresos - gastos)
     */
    private fun calcularSaldoTotal(transacciones: List<Transaccion>): Double {
        var total = 0.0
        transacciones.forEach { transaccion ->
            if (transaccion.esGasto) {
                total -= transaccion.cantidad
            } else {
                total += transaccion.cantidad
            }
        }
        return total
    }
    
    /**
     * Calcula el total de gastos del mes actual
     */
    private fun calcularGastosMes(transacciones: List<Transaccion>): Double {
        val ahora = System.currentTimeMillis()
        val inicioMes = obtenerInicioMes(ahora)
        
        return transacciones
            .filter { it.esGasto && it.fecha >= inicioMes }
            .sumOf { it.cantidad }
    }
    
    /**
     * Obtiene el timestamp del inicio del mes actual
     */
    private fun obtenerInicioMes(timestamp: Long): Long {
        val calendario = java.util.Calendar.getInstance()
        calendario.timeInMillis = timestamp
        calendario.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendario.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendario.set(java.util.Calendar.MINUTE, 0)
        calendario.set(java.util.Calendar.SECOND, 0)
        calendario.set(java.util.Calendar.MILLISECOND, 0)
        return calendario.timeInMillis
    }
    
    /**
     * Borra una transacción
     */
    fun borrarTransaccion(transaccion: Transaccion) {
        viewModelScope.launch {
            try {
                repositorioFinanzas.borrarTransaccion(transaccion)
                // No es necesario recargar, el Flow se actualiza automáticamente
            } catch (e: Exception) {
                _estado.value = EstadoInicio.Error("Error al borrar: ${e.message}")
            }
        }
    }
}
