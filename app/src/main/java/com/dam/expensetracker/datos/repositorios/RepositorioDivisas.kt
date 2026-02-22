package com.dam.expensetracker.datos.repositorios

import com.dam.expensetracker.datos.remoto.api.ApiDivisas
import com.dam.expensetracker.datos.remoto.dto.ConversionDto
import com.dam.expensetracker.datos.remoto.dto.TipoCambioDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio que gestiona las llamadas a la API de tipos de cambio
 */
class RepositorioDivisas(
    private val api: ApiDivisas
) {
    
    /**
     * Obtiene los tipos de cambio actuales para una moneda base
     * @param moneda Código de la moneda base (EUR, USD, etc.)
     * @return TipoCambioDto o null si hay error
     */
    suspend fun obtenerTiposCambio(moneda: String = "EUR"): TipoCambioDto? {
        return withContext(Dispatchers.IO) {
            try {
                val respuesta = api.obtenerTiposCambio(moneda)
                if (respuesta.isSuccessful) {
                    respuesta.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * Convierte una cantidad de una moneda a otra
     * @return ConversionDto con el resultado o null si hay error
     */
    suspend fun convertirDivisa(
        monedaOrigen: String,
        monedaDestino: String,
        cantidad: Double
    ): ConversionDto? {
        return withContext(Dispatchers.IO) {
            try {
                val respuesta = api.obtenerTiposCambio(monedaOrigen)
                if (respuesta.isSuccessful) {
                    val tipoCambio = respuesta.body()
                    val tasa = tipoCambio?.tasas?.get(monedaDestino)
                    
                    if (tasa != null) {
                        ConversionDto(
                            monedaOrigen = monedaOrigen,
                            monedaDestino = monedaDestino,
                            tasa = tasa,
                            cantidad = cantidad,
                            resultado = cantidad * tasa
                        )
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * Obtiene los tipos de cambio del dólar (segundo endpoint de ejemplo)
     */
    suspend fun obtenerTiposDolar(): TipoCambioDto? {
        return withContext(Dispatchers.IO) {
            try {
                val respuesta = api.obtenerTiposDolar()
                if (respuesta.isSuccessful) {
                    respuesta.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
