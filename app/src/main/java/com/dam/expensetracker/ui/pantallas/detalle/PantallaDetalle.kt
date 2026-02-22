package com.dam.expensetracker.ui.pantallas.detalle

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla que muestra los detalles de una transacción
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalle(
    transaccionId: Long,
    onNavegarAtras: () -> Unit,
    onNavegarEditar: (Long) -> Unit,
    viewModel: DetalleViewModel
) {
    val estado by viewModel.estado.collectAsState()
    var mostrarDialogoBorrar by remember { mutableStateOf(false) }
    
    // Cargar los datos cuando se crea la pantalla
    LaunchedEffect(transaccionId) {
        viewModel.cargarTransaccion(transaccionId)
    }
    
    // Navegar atrás cuando se borre
    LaunchedEffect(estado) {
        if (estado is EstadoDetalle.Borrado) {
            onNavegarAtras()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de transacción") },
                navigationIcon = {
                    IconButton(onClick = onNavegarAtras) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (estado is EstadoDetalle.Exito) {
                        IconButton(onClick = { onNavegarEditar(transaccionId) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar"
                            )
                        }
                        IconButton(onClick = { mostrarDialogoBorrar = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Borrar"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val estadoActual = estado) {
            is EstadoDetalle.Cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is EstadoDetalle.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = estadoActual.mensaje,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            is EstadoDetalle.Exito -> {
                val transaccion = estadoActual.transaccion
                val categoria = estadoActual.categoria
                val cuenta = estadoActual.cuenta
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tarjeta principal con la cantidad
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (transaccion.esGasto) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (transaccion.esGasto) "Gasto" else "Ingreso",
                                style = MaterialTheme.typography.labelLarge
                            )
                            
                            Text(
                                text = "${if (transaccion.esGasto) "-" else "+"}${
                                    String.format("%.2f", transaccion.cantidad)
                                }€",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Detalles
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CampoDetalle(
                                etiqueta = "Categoría",
                                valor = categoria?.nombre ?: "Sin categoría"
                            )
                            
                            HorizontalDivider()
                            
                            CampoDetalle(
                                etiqueta = "Cuenta",
                                valor = cuenta?.nombre ?: "Sin cuenta"
                            )
                            
                            HorizontalDivider()
                            
                            CampoDetalle(
                                etiqueta = "Fecha",
                                valor = formatearFecha(transaccion.fecha)
                            )
                            
                            if (transaccion.nota.isNotBlank()) {
                                HorizontalDivider()
                                
                                CampoDetalle(
                                    etiqueta = "Nota",
                                    valor = transaccion.nota
                                )
                            }
                        }
                    }
                }
                
                // Diálogo de confirmación para borrar
                if (mostrarDialogoBorrar) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoBorrar = false },
                        title = { Text("Confirmar borrado") },
                        text = { Text("¿Estás seguro de que quieres borrar esta transacción?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.borrarTransaccion(transaccion)
                                    mostrarDialogoBorrar = false
                                }
                            ) {
                                Text("Borrar", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarDialogoBorrar = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
            
            is EstadoDetalle.Borrado -> {
                // Estado manejado con LaunchedEffect
            }
        }
    }
}

@Composable
private fun CampoDetalle(
    etiqueta: String,
    valor: String
) {
    Column {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        @Suppress("DEPRECATION")
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatearFecha(timestamp: Long): String {
    val formato = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    return formato.format(Date(timestamp))
}
