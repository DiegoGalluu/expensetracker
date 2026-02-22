package com.dam.expensetracker.ui.pantallas.presupuestos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.expensetracker.datos.local.entidades.Categoria
import com.dam.expensetracker.datos.local.entidades.Presupuesto
import com.dam.expensetracker.datos.local.entidades.Transaccion
import com.dam.expensetracker.datos.repositorios.RepositorioFinanzas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

data class PresupuestoCategoriaUi(
    val idCategoria: Long,
    val nombreCategoria: String,
    val limiteMensual: Double,
    val gastoMesActual: Double
)

sealed class EstadoPresupuestos {
    object Cargando : EstadoPresupuestos()
    data class Exito(
        val presupuestos: List<PresupuestoCategoriaUi>,
        val ahorroActualMes: Double,
        val metaAhorroMensual: Double
    ) : EstadoPresupuestos()
    data class Error(val mensaje: String) : EstadoPresupuestos()
}

class PresupuestosViewModel(
    private val repositorio: RepositorioFinanzas
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoPresupuestos>(EstadoPresupuestos.Cargando)
    val estado: StateFlow<EstadoPresupuestos> = _estado.asStateFlow()

    private val _metaAhorroMensual = MutableStateFlow(300.0)

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            try {
                combine(
                    repositorio.obtenerTodasCategorias(),
                    repositorio.obtenerTodosPresupuestos(),
                    repositorio.obtenerTodasTransacciones(),
                    _metaAhorroMensual
                ) { categorias, presupuestos, transacciones, metaAhorro ->
                    construirEstado(categorias, presupuestos, transacciones, metaAhorro)
                }.collect { nuevoEstado ->
                    _estado.value = nuevoEstado
                }
            } catch (e: Exception) {
                _estado.value = EstadoPresupuestos.Error("Error al cargar presupuestos: ${e.message}")
            }
        }
    }

    fun guardarPresupuestoCategoria(idCategoria: Long, nuevoLimiteTexto: String) {
        viewModelScope.launch {
            try {
                val nuevoLimite = nuevoLimiteTexto
                    .trim()
                    .replace(',', '.')
                    .toDoubleOrNull()

                if (nuevoLimite == null || nuevoLimite < 0) {
                    _estado.value = EstadoPresupuestos.Error("El presupuesto debe ser un número válido mayor o igual a 0")
                    return@launch
                }

                val presupuestoExistente = repositorio.obtenerPresupuestoPorCategoria(idCategoria)

                if (presupuestoExistente == null) {
                    repositorio.insertarPresupuesto(
                        Presupuesto(
                            idCategoria = idCategoria,
                            limiteMensual = nuevoLimite
                        )
                    )
                } else {
                    repositorio.actualizarPresupuesto(
                        presupuestoExistente.copy(limiteMensual = nuevoLimite)
                    )
                }
            } catch (e: Exception) {
                _estado.value = EstadoPresupuestos.Error("Error al guardar presupuesto: ${e.message}")
            }
        }
    }

    fun actualizarMetaAhorroMensual(metaTexto: String) {
        val meta = metaTexto
            .trim()
            .replace(',', '.')
            .toDoubleOrNull()

        if (meta != null && meta >= 0) {
            _metaAhorroMensual.value = meta
        }
    }

    private fun construirEstado(
        categorias: List<Categoria>,
        presupuestos: List<Presupuesto>,
        transacciones: List<Transaccion>,
        metaAhorro: Double
    ): EstadoPresupuestos {
        val (inicioMes, finMes) = rangoMesActual()

        val transaccionesMes = transacciones.filter { it.fecha >= inicioMes && it.fecha < finMes }

        val gastosPorCategoria = transaccionesMes
            .filter { it.esGasto }
            .groupBy { it.idCategoria }
            .mapValues { entry -> entry.value.sumOf { it.cantidad } }

        val mapaPresupuestos = presupuestos.associateBy { it.idCategoria }

        val listaUi = categorias.map { categoria ->
            val presupuesto = mapaPresupuestos[categoria.id]
            PresupuestoCategoriaUi(
                idCategoria = categoria.id,
                nombreCategoria = categoria.nombre,
                limiteMensual = presupuesto?.limiteMensual ?: 0.0,
                gastoMesActual = gastosPorCategoria[categoria.id] ?: 0.0
            )
        }.sortedBy { it.nombreCategoria }

        val ingresosMes = transaccionesMes
            .filter { !it.esGasto }
            .sumOf { it.cantidad }

        val gastosMes = transaccionesMes
            .filter { it.esGasto }
            .sumOf { it.cantidad }

        val ahorroActual = ingresosMes - gastosMes

        return EstadoPresupuestos.Exito(
            presupuestos = listaUi,
            ahorroActualMes = ahorroActual,
            metaAhorroMensual = metaAhorro
        )
    }

    private fun rangoMesActual(): Pair<Long, Long> {
        val calendario = Calendar.getInstance()

        calendario.set(Calendar.DAY_OF_MONTH, 1)
        calendario.set(Calendar.HOUR_OF_DAY, 0)
        calendario.set(Calendar.MINUTE, 0)
        calendario.set(Calendar.SECOND, 0)
        calendario.set(Calendar.MILLISECOND, 0)
        val inicioMes = calendario.timeInMillis

        calendario.add(Calendar.MONTH, 1)
        val finMes = calendario.timeInMillis

        return Pair(inicioMes, finMes)
    }
}
