package com.dam.expensetracker.ui.tema

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Esquema de colores claro
 */
private val EsquemaColorClaro = lightColorScheme(
    primary = VerdePrincipal,
    onPrimary = TextoBlanco,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = TextoPrincipal,
    
    secondary = AzulSecundario,
    onSecondary = TextoBlanco,
    secondaryContainer = Color(0xFFBBDEFB),
    onSecondaryContainer = TextoPrincipal,
    
    error = Rojo,
    onError = TextoBlanco,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = RojoOscuro,
    
    background = FondoClaro,
    onBackground = TextoPrincipal,
    
    surface = SuperficieClara,
    onSurface = TextoPrincipal,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = TextoSecundario
)

/**
 * Esquema de colores oscuro
 */
private val EsquemaColorOscuro = darkColorScheme(
    primary = VerdePrincipal,
    onPrimary = TextoPrincipal,
    primaryContainer = VerdePrincipalOscuro,
    onPrimaryContainer = TextoBlanco,
    
    secondary = AzulSecundario,
    onSecondary = TextoPrincipal,
    secondaryContainer = AzulSecundarioOscuro,
    onSecondaryContainer = TextoBlanco,
    
    error = Color(0xFFEF5350),
    onError = TextoPrincipal,
    errorContainer = RojoOscuro,
    onErrorContainer = Color(0xFFFFCDD2),
    
    background = FondoOscuro,
    onBackground = TextoBlanco,
    
    surface = SuperficieOscura,
    onSurface = TextoBlanco,
    surfaceVariant = Color(0xFF424242),
    onSurfaceVariant = Color(0xFFBDBDBD)
)

/**
 * Tema principal de la aplicación
 */
@Composable
fun TemaExpenseTracker(
    modoOscuro: Boolean = isSystemInDarkTheme(),
    contenido: @Composable () -> Unit
) {
    val colores = if (modoOscuro) {
        EsquemaColorOscuro
    } else {
        EsquemaColorClaro
    }
    
    MaterialTheme(
        colorScheme = colores,
        typography = TipografiaApp,
        content = contenido
    )
}
