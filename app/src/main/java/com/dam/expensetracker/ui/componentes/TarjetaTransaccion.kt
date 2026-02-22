package com.dam.expensetracker.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dam.expensetracker.datos.local.entidades.Transaccion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tarjeta que muestra una transacción en la lista
 */
@Composable
fun TarjetaTransaccion(
    transaccion: Transaccion,
    nombreCategoria: String,
    nombreCuenta: String,
    colorCategoria: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Parte izquierda: icono de categoría y detalles
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Círculo con color de categoría
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colorCategoria),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaccion.esGasto) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = if (transaccion.esGasto) "Gasto" else "Ingreso",
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Información de la transacción
                Column {
                    Text(
                        text = nombreCategoria,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (transaccion.nota.isNotBlank()) {
                        Text(
                            text = transaccion.nota,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    
                    Text(
                        text = formatearFecha(transaccion.fecha),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = nombreCuenta,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Parte derecha: cantidad
            Text(
                text = "${if (transaccion.esGasto) "-" else "+"}${String.format("%.2f", transaccion.cantidad)}€",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (transaccion.esGasto) {
                    Color(0xFFF44336) // Rojo para gastos
                } else {
                    Color(0xFF4CAF50) // Verde para ingresos
                }
            )
        }
    }
}

/**
 * Formatea un timestamp a formato legible
 */
private fun formatearFecha(timestamp: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
    return formato.format(Date(timestamp))
}
