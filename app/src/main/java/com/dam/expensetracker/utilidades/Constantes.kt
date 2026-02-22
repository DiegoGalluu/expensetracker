package com.dam.expensetracker.utilidades

/**
 * Constantes globales de la aplicación
 */
object Constantes {
    
    // URL base de la API de tipos de cambio
    // Esta es una API pública gratuita, no requiere API key
    const val BASE_URL_API_DIVISAS = "https://api.exchangerate-api.com/v4/"
    
    // Códigos de monedas más comunes
    const val MONEDA_EUR = "EUR"
    const val MONEDA_USD = "USD"
    const val MONEDA_GBP = "GBP"
    
    // Formato de fecha para mostrar
    const val FORMATO_FECHA = "dd/MM/yyyy"
    
    // Rutas de navegación
    object Rutas {
        const val LOGIN = "login"
        const val INICIO = "inicio"
        const val DETALLE = "detalle/{transaccionId}"
        const val FORMULARIO = "formulario?transaccionId={transaccionId}"
        
        // Funciones helper para construir rutas con parámetros
        fun detalleConId(transaccionId: Long) = "detalle/$transaccionId"
        fun formularioConId(transaccionId: Long? = null) = 
            if (transaccionId != null) "formulario?transaccionId=$transaccionId" 
            else "formulario"
    }
}
