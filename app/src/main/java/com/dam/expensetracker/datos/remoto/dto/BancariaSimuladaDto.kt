package com.dam.expensetracker.datos.remoto.dto

import com.google.gson.annotations.SerializedName

data class CestaBancariaDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("total")
    val total: Double,
    @SerializedName("discountedTotal")
    val totalDescontado: Double,
    @SerializedName("products")
    val movimientos: List<MovimientoBancarioDto>
)

data class MovimientoBancarioDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val concepto: String,
    @SerializedName("quantity")
    val cantidadItems: Int,
    @SerializedName("total")
    val importe: Double
)

data class ResumenBancarioSimulado(
    val saldoDisponible: Double,
    val gastosUltimosMovimientos: Double,
    val movimientos: List<MovimientoBancarioUi>
)

data class MovimientoBancarioUi(
    val id: Long,
    val concepto: String,
    val importe: Double,
    val detalle: String
)
