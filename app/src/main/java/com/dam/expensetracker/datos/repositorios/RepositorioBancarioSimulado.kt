package com.dam.expensetracker.datos.repositorios

import com.dam.expensetracker.datos.remoto.api.ApiBancariaSimulada
import com.dam.expensetracker.datos.remoto.dto.MovimientoBancarioUi
import com.dam.expensetracker.datos.remoto.dto.ResumenBancarioSimulado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositorioBancarioSimulado(
    private val api: ApiBancariaSimulada
) {

    suspend fun obtenerResumenBancario(): ResumenBancarioSimulado? {
        return withContext(Dispatchers.IO) {
            try {
                val respuesta = api.obtenerResumenBancario()
                val cuerpo = if (respuesta.isSuccessful) respuesta.body() else null
                if (cuerpo == null) return@withContext null

                val saldoSemilla = 5000.0
                val saldoDisponible = (saldoSemilla - cuerpo.totalDescontado).coerceAtLeast(0.0)

                val movimientos = cuerpo.movimientos.map { movimiento ->
                    val conceptoOriginal = movimiento.concepto
                    val conceptoLower = conceptoOriginal.lowercase()

                    val conceptoAjustado = when {
                        conceptoLower.contains("charger") -> "charger sxt rwd"
                        conceptoLower.contains("earring") || conceptoLower.contains("pendiente") -> "PENDIENTES"
                        else -> conceptoOriginal
                    }

                    val cantidadAjustada = when {
                        conceptoLower.contains("charger") -> 1
                        conceptoLower.contains("airpod") -> 2
                        else -> movimiento.cantidadItems
                    }

                    MovimientoBancarioUi(
                        id = movimiento.id,
                        concepto = conceptoAjustado,
                        importe = movimiento.importe,
                        detalle = "${cantidadAjustada} uds"
                    )
                }

                ResumenBancarioSimulado(
                    saldoDisponible = saldoDisponible,
                    gastosUltimosMovimientos = cuerpo.totalDescontado,
                    movimientos = movimientos
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
