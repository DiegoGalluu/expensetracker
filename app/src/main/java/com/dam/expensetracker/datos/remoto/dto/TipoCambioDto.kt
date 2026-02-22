package com.dam.expensetracker.datos.remoto.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO que representa la respuesta de la API de tipos de cambio
 * Ejemplo de API: https://api.exchangerate-api.com/v4/latest/EUR
 */
data class TipoCambioDto(
    @SerializedName("base")
    val monedaBase: String,  // Ejemplo: "EUR"
    
    @SerializedName("date")
    val fecha: String,  // Fecha de actualización
    
    @SerializedName("rates")
    val tasas: Map<String, Double>  // Mapa de monedas y sus tasas (USD: 1.08, etc.)
)

/**
 * DTO simplificado para un tipo de cambio específico
 */
data class ConversionDto(
    val monedaOrigen: String,
    val monedaDestino: String,
    val tasa: Double,
    val cantidad: Double,
    val resultado: Double
)
