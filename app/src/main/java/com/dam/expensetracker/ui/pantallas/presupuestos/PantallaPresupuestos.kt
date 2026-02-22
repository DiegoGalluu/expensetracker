package com.dam.expensetracker.ui.pantallas.presupuestos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPresupuestos(
    onNavegarAtras: () -> Unit,
    viewModel: PresupuestosViewModel
) {
    val estado by viewModel.estado.collectAsState()
    val valoresEditados = remember { mutableStateMapOf<Long, String>() }
    var metaTexto by remember { mutableStateOf("300.0") }

    LaunchedEffect(estado) {
        val estadoActual = estado
        if (estadoActual is EstadoPresupuestos.Exito) {
            estadoActual.presupuestos.forEach { item ->
                valoresEditados.putIfAbsent(item.idCategoria, item.limiteMensual.toString())
            }
            metaTexto = estadoActual.metaAhorroMensual.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Presupuestos y metas") },
                navigationIcon = {
                    IconButton(onClick = onNavegarAtras) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val estadoActual = estado) {
            is EstadoPresupuestos.Cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is EstadoPresupuestos.Error -> {
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
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            is EstadoPresupuestos.Exito -> {
                ContenidoPresupuestos(
                    paddingValues = paddingValues,
                    estado = estadoActual,
                    valoresEditados = valoresEditados,
                    metaTexto = metaTexto,
                    onMetaTextoChange = { metaTexto = it },
                    onGuardarMeta = { viewModel.actualizarMetaAhorroMensual(metaTexto) },
                    onGuardarPresupuesto = { idCategoria ->
                        val valor = valoresEditados[idCategoria] ?: "0"
                        viewModel.guardarPresupuestoCategoria(idCategoria, valor)
                    },
                    onLimiteChange = { idCategoria, nuevoValor ->
                        valoresEditados[idCategoria] = nuevoValor
                    }
                )
            }
        }
    }
}

@Composable
private fun ContenidoPresupuestos(
    paddingValues: PaddingValues,
    estado: EstadoPresupuestos.Exito,
    valoresEditados: MutableMap<Long, String>,
    metaTexto: String,
    onMetaTextoChange: (String) -> Unit,
    onGuardarMeta: () -> Unit,
    onGuardarPresupuesto: (Long) -> Unit,
    onLimiteChange: (Long, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            TarjetaMetaAhorro(
                ahorroActual = estado.ahorroActualMes,
                metaAhorro = estado.metaAhorroMensual,
                metaTexto = metaTexto,
                onMetaTextoChange = onMetaTextoChange,
                onGuardarMeta = onGuardarMeta
            )
        }

        item {
            Text(
                text = "Presupuesto mensual por categoría",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(estado.presupuestos, key = { it.idCategoria }) { item ->
            val textoLimite = valoresEditados[item.idCategoria] ?: item.limiteMensual.toString()
            TarjetaPresupuestoCategoria(
                item = item,
                limiteTexto = textoLimite,
                onLimiteChange = { onLimiteChange(item.idCategoria, it) },
                onGuardar = { onGuardarPresupuesto(item.idCategoria) }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun TarjetaMetaAhorro(
    ahorroActual: Double,
    metaAhorro: Double,
    metaTexto: String,
    onMetaTextoChange: (String) -> Unit,
    onGuardarMeta: () -> Unit
) {
    val progreso = if (metaAhorro <= 0.0) 0f else (ahorroActual / metaAhorro).toFloat().coerceIn(0f, 1f)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Meta de ahorro mensual",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ahorro actual del mes: ${String.format("%.2f€", ahorroActual)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Meta actual: ${String.format("%.2f€", metaAhorro)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = metaTexto,
                onValueChange = onMetaTextoChange,
                label = { Text("Nueva meta (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onGuardarMeta,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar meta")
            }
        }
    }
}

@Composable
private fun TarjetaPresupuestoCategoria(
    item: PresupuestoCategoriaUi,
    limiteTexto: String,
    onLimiteChange: (String) -> Unit,
    onGuardar: () -> Unit
) {
    val porcentajeUso = if (item.limiteMensual <= 0.0) 0f else (item.gastoMesActual / item.limiteMensual).toFloat()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.nombreCategoria,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Gasto este mes: ${String.format("%.2f€", item.gastoMesActual)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Límite actual: ${String.format("%.2f€", item.limiteMensual)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { porcentajeUso.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )

            if (porcentajeUso > 1f) {
                Text(
                    text = "Has superado el presupuesto de esta categoría",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = limiteTexto,
                    onValueChange = onLimiteChange,
                    label = { Text("Nuevo límite") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Button(onClick = onGuardar) {
                    Text("Guardar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
        }
    }
}
