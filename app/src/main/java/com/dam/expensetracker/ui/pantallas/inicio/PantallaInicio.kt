package com.dam.expensetracker.ui.pantallas.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.dam.expensetracker.datos.local.entidades.Categoria
import com.dam.expensetracker.datos.local.entidades.Transaccion
import com.dam.expensetracker.ui.componentes.DatosGrafico
import com.dam.expensetracker.ui.componentes.GraficoCircular
import com.dam.expensetracker.ui.componentes.TarjetaTransaccion
import kotlinx.coroutines.launch

/**
 * Pantalla de inicio/dashboard que muestra resumen financiero y lista de transacciones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    onNavegarDetalle: (Long) -> Unit,
    onNavegarFormulario: () -> Unit,
    onNavegarMetas: () -> Unit,
    onNavegarPresupuestos: () -> Unit,
    onNavegarCuentas: () -> Unit,
    onNavegarRecurrentes: () -> Unit,
    onNavegarDivisas: () -> Unit,
    onCerrarSesion: () -> Unit,
    emailUsuario: String,
    viewModel: InicioViewModel
) {
    val estado by viewModel.estado.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ExpenseTracker",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = emailUsuario,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = onCerrarSesion) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Cerrar sesión"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    NavigationDrawerItem(
                        label = { Text("Metas") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavegarMetas()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Metas"
                            )
                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("Presupuestos") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavegarPresupuestos()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Presupuestos"
                            )
                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("Cuentas") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavegarCuentas()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = "Cuentas"
                            )
                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("Recurrentes") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavegarRecurrentes()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Recurrentes"
                            )
                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("Divisas") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavegarDivisas()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Divisas"
                            )
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ExpenseTracker") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir menú"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavegarFormulario,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir transacción",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        ) { paddingValues ->
            when (val estadoActual = estado) {
                is EstadoInicio.Cargando -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is EstadoInicio.Error -> {
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

                is EstadoInicio.Exito -> {
                    ContenidoInicio(
                        transacciones = estadoActual.transacciones,
                        categorias = estadoActual.categorias,
                        saldoTotal = estadoActual.saldoTotal,
                        totalGastosMes = estadoActual.totalGastosMes,
                        onNavegarDetalle = onNavegarDetalle,
                        paddingValues = paddingValues
                    )
                }
            }
        }
    }
}

@Composable
private fun ContenidoInicio(
    transacciones: List<Transaccion>,
    categorias: List<Categoria>,
    saldoTotal: Double,
    totalGastosMes: Double,
    onNavegarDetalle: (Long) -> Unit,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Espacio superior
        item { Spacer(modifier = Modifier.height(8.dp)) }
        
        // Tarjeta de resumen de saldo
        item {
            TarjetaResumenSaldo(
                saldoTotal = saldoTotal,
                totalGastosMes = totalGastosMes
            )
        }
        
        // Gráfico de gastos por categoría
        item {
            TarjetaGrafico(
                transacciones = transacciones,
                categorias = categorias
            )
        }
        
        // Título de transacciones recientes
        item {
            Text(
                text = "Transacciones recientes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Lista de transacciones
        if (transacciones.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No hay transacciones aún. Pulsa el botón + para añadir una.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(transacciones) { transaccion ->
                val categoria = categorias.find { it.id == transaccion.idCategoria }
                
                TarjetaTransaccion(
                    transaccion = transaccion,
                    nombreCategoria = categoria?.nombre ?: "Sin categoría",
                    nombreCuenta = "Cuenta", // En una versión más completa, buscaríamos esto
                    colorCategoria = try {
                        Color((categoria?.color?.toColorInt() ?: 0xFF757575).toLong())
                    } catch (e: Exception) {
                        Color(0xFF757575L)
                    },
                    onClick = { onNavegarDetalle(transaccion.id) }
                )
            }
        }
        
        // Espacio inferior
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun TarjetaResumenSaldo(
    saldoTotal: Double,
    totalGastosMes: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Saldo total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = String.format("%.2f€", saldoTotal),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Gastos este mes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = String.format("-%.2f€", totalGastosMes),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaGrafico(
    transacciones: List<Transaccion>,
    categorias: List<Categoria>
) {
    // Filtrar solo gastos del mes actual
    val ahora = System.currentTimeMillis()
    val calendario = java.util.Calendar.getInstance()
    calendario.timeInMillis = ahora
    calendario.set(java.util.Calendar.DAY_OF_MONTH, 1)
    calendario.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendario.set(java.util.Calendar.MINUTE, 0)
    calendario.set(java.util.Calendar.SECOND, 0)
    calendario.set(java.util.Calendar.MILLISECOND, 0)
    val inicioMes = calendario.timeInMillis
    
    val gastosMes = transacciones.filter { it.esGasto && it.fecha >= inicioMes }
    
    // Agrupar gastos por categoría
    val gastosPorCategoria = gastosMes
        .groupBy { it.idCategoria }
        .mapValues { entry -> entry.value.sumOf { it.cantidad } }
    
    // Convertir a datos para el gráfico
    val datosGrafico = gastosPorCategoria.map { (idCategoria, total) ->
        val categoria = categorias.find { it.id == idCategoria }
        DatosGrafico(
            nombre = categoria?.nombre ?: "Otros",
            valor = total,
            color = try {
                Color((categoria?.color?.toColorInt() ?: 0xFF757575).toLong())
            } catch (e: Exception) {
                Color(0xFF757575L)
            }
        )
    }.sortedByDescending { it.valor }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Gastos por categoría (este mes)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            GraficoCircular(datos = datosGrafico)
        }
    }
}
