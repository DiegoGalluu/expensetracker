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
import kotlin.random.Random

data class PresupuestoCategoriaUi(
    val idCategoria: Long,
    val nombreCategoria: String,
    val limiteMensual: Double,
    val gastoMesActual: Double
)

data class MetaPersonalizadaUi(
    val id: Long,
    val nombre: String,
    val objetivo: Double,
    val progresoActual: Double
)

sealed class EstadoPresupuestos {
    object Cargando : EstadoPresupuestos()
    data class Exito(
        val presupuestos: List<PresupuestoCategoriaUi>,
        val ahorroActualMes: Double,
        val metaAhorroMensual: Double,
        val metasPersonalizadas: List<MetaPersonalizadaUi>
    ) : EstadoPresupuestos()
    data class Error(val mensaje: String) : EstadoPresupuestos()
}

class PresupuestosViewModel(
    private val repositorio: RepositorioFinanzas
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoPresupuestos>(EstadoPresupuestos.Cargando)
    val estado: StateFlow<EstadoPresupuestos> = _estado.asStateFlow()

    private val _metaAhorroMensual = MutableStateFlow(300.0)
    private val _metasPersonalizadas = MutableStateFlow<List<MetaPersonalizadaUi>>(emptyList())

    private val categoriasBase = setOf(
        "Comida", "Transporte", "Ocio", "Salud", "Vivienda", "Educación", "Otros"
    )

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
                    _metaAhorroMensual,
                    _metasPersonalizadas
                ) { categorias, presupuestos, transacciones, metaAhorro, metasPersonalizadas ->
                    construirEstado(
                        categorias = categorias,
                        presupuestos = presupuestos,
                        transacciones = transacciones,
                        metaAhorro = metaAhorro,
                        metasPersonalizadas = metasPersonalizadas
                    )
                }.collect { nuevoEstado ->
                    _estado.value = nuevoEstado
                }
            } catch (e: Exception) {
                _estado.value = EstadoPresupuestos.Error("Error al cargar presupuestos: ${e.message}")
            }
        }
    }

    fun crearPresupuestoPersonalizado(nombre: String, limiteTexto: String) {
        viewModelScope.launch {
            try {
                val nombreNormalizado = nombre.trim()
                val limite = limiteTexto.trim().replace(',', '.').toDoubleOrNull()

                if (nombreNormalizado.isBlank()) {
                    _estado.value = EstadoPresupuestos.Error("El nombre del presupuesto es obligatorio")
                    return@launch
                }

                if (limite == null || limite < 0) {
                    _estado.value = EstadoPresupuestos.Error("El límite del presupuesto debe ser válido")
                    return@launch
                }

                val nuevaCategoriaId = repositorio.insertarCategoria(
                    Categoria(
                        nombre = nombreNormalizado,
                        color = colorAleatorioHex()
                    )
                )

                repositorio.insertarPresupuesto(
                    Presupuesto(
                        idCategoria = nuevaCategoriaId,
                        limiteMensual = limite
                    )
                )
            } catch (e: Exception) {
                _estado.value = EstadoPresupuestos.Error("No se pudo crear el presupuesto: ${e.message}")
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

    fun crearMetaPersonalizada(nombre: String, objetivoTexto: String) {
        val nombreNormalizado = nombre.trim()
        val objetivo = objetivoTexto.trim().replace(',', '.').toDoubleOrNull()

        if (nombreNormalizado.isBlank() || objetivo == null || objetivo <= 0) {
            _estado.value = EstadoPresupuestos.Error("La meta debe tener nombre y objetivo válido")
            return
        }

        val nuevaMeta = MetaPersonalizadaUi(
            id = System.currentTimeMillis(),
            nombre = nombreNormalizado,
            objetivo = objetivo,
            progresoActual = 0.0
        )

        _metasPersonalizadas.value = _metasPersonalizadas.value + nuevaMeta
    }

    fun actualizarProgresoMeta(idMeta: Long, progresoTexto: String) {
        val progreso = progresoTexto.trim().replace(',', '.').toDoubleOrNull()

        if (progreso == null || progreso < 0) {
            _estado.value = EstadoPresupuestos.Error("El progreso de la meta no es válido")
            return
        }

        _metasPersonalizadas.value = _metasPersonalizadas.value.map {
            if (it.id == idMeta) it.copy(progresoActual = progreso) else it
        }
    }

    private fun construirEstado(
        categorias: List<Categoria>,
        presupuestos: List<Presupuesto>,
        transacciones: List<Transaccion>,
        metaAhorro: Double,
        metasPersonalizadas: List<MetaPersonalizadaUi>
    ): EstadoPresupuestos {
        val (inicioMes, finMes) = rangoMesActual()

        val transaccionesMes = transacciones.filter { it.fecha >= inicioMes && it.fecha < finMes }

        val gastosPorCategoria = transaccionesMes
            .filter { it.esGasto }
            .groupBy { it.idCategoria }
            .mapValues { entry -> entry.value.sumOf { it.cantidad } }

        val listaUi = presupuestos.mapNotNull { presupuesto ->
            val categoria = categorias.find { it.id == presupuesto.idCategoria } ?: return@mapNotNull null
            if (categoria.nombre in categoriasBase) return@mapNotNull null

            PresupuestoCategoriaUi(
                idCategoria = categoria.id,
                nombreCategoria = categoria.nombre,
                limiteMensual = presupuesto.limiteMensual,
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
            metaAhorroMensual = metaAhorro,
            metasPersonalizadas = metasPersonalizadas
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

    private fun colorAleatorioHex(): String {
        val numero = Random.nextInt(0x1000000)
        return String.format("#%06X", numero)
    }
}
