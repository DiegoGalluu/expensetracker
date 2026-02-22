package com.dam.expensetracker.datos.remoto.api

import com.dam.expensetracker.datos.remoto.dto.TipoCambioDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interfaz de Retrofit para consumir la API de tipos de cambio
 * URL base: https://api.exchangerate-api.com/v4/
 * 
 * NOTA: Esta es una API pública gratuita sin necesidad de API key
 */
interface ApiDivisas {
    
    /**
     * Obtener los tipos de cambio actuales para una moneda base
     * Ejemplo: GET /latest/EUR devuelve todas las tasas con EUR como base
     */
    @GET("latest/{moneda}")
    suspend fun obtenerTiposCambio(
        @Path("moneda") moneda: String = "EUR"
    ): Response<TipoCambioDto>
    
    /**
     * Alternativa: obtener tipos específicos (solo para demostración)
     * En una API real podría ser algo como /convert?from=EUR&to=USD&amount=100
     */
    @GET("latest/USD")
    suspend fun obtenerTiposDolar(): Response<TipoCambioDto>
}
