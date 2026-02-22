package com.dam.expensetracker.datos.remoto.api

import com.dam.expensetracker.datos.remoto.dto.CestaBancariaDto
import retrofit2.Response
import retrofit2.http.GET

/**
 * API bancaria simulada para demo académica.
 * Se reutiliza un backend público con estructura de movimientos de compra.
 */
interface ApiBancariaSimulada {

    @GET("carts/1")
    suspend fun obtenerResumenBancario(): Response<CestaBancariaDto>
}
