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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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

enum class ModoPresupuestos {
    METAS,
    PRESUPUESTOS
}

private sealed class ConfirmacionBorrado {
    data class Meta(val id: Long, val nombre: String) : ConfirmacionBorrado()
    data class Presupuesto(val idCategoria: Long, val nombre: String) : ConfirmacionBorrado()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPresupuestos(
    onNavegarAtras: () -> Unit,
    modo: ModoPresupuestos,
    viewModel: PresupuestosViewModel
) {
    val estado by viewModel.estado.collectAsState()
    val valoresEditados = remember { mutableStateMapOf<Long, String>() }
    val progresoMetaEditado = remember { mutableStateMapOf<Long, String>() }
    var metaTexto by remember { mutableStateOf("300.0") }
    var nombrePresupuestoNuevo by remember { mutableStateOf("") }
    var limitePresupuestoNuevo by remember { mutableStateOf("") }
    var nombreMetaNueva by remember { mutableStateOf("") }
    var objetivoMetaNueva by remember { mutableStateOf("") }
    var confirmacionBorrado by remember { mutableStateOf<ConfirmacionBorrado?>(null) }

    LaunchedEffect(estado) {
        val estadoActual = estado
        if (estadoActual is EstadoPresupuestos.Exito) {
            estadoActual.presupuestos.forEach { item ->
                valoresEditados.putIfAbsent(item.idCategoria, item.limiteMensual.toString())
            }
            estadoActual.metasPersonalizadas.forEach { meta ->
                progresoMetaEditado.putIfAbsent(meta.id, meta.progresoActual.toString())
            }
            metaTexto = estadoActual.metaAhorroMensual.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (modo == ModoPresupuestos.METAS) "Metas" else "Presupuestos"
                    )
                },
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
                    modo = modo,
                    estado = estadoActual,
                    valoresEditados = valoresEditados,
                    metaTexto = metaTexto,
                    onMetaTextoChange = { metaTexto = it },
                    onGuardarMeta = { viewModel.actualizarMetaAhorroMensual(metaTexto) },
                    nombrePresupuestoNuevo = nombrePresupuestoNuevo,
                    limitePresupuestoNuevo = limitePresupuestoNuevo,
                    onNombrePresupuestoNuevoChange = { nombrePresupuestoNuevo = it },
                    onLimitePresupuestoNuevoChange = { limitePresupuestoNuevo = it },
                    onCrearPresupuesto = {
                        viewModel.crearPresupuestoPersonalizado(nombrePresupuestoNuevo, limitePresupuestoNuevo)
                        nombrePresupuestoNuevo = ""
                        limitePresupuestoNuevo = ""
                    },
                    onGuardarPresupuesto = { idCategoria ->
                        val valor = valoresEditados[idCategoria] ?: "0"
                        viewModel.guardarPresupuestoCategoria(idCategoria, valor)
                    },
                    onLimiteChange = { idCategoria, nuevoValor ->
                        valoresEditados[idCategoria] = nuevoValor
                    },
                    nombreMetaNueva = nombreMetaNueva,
                    objetivoMetaNueva = objetivoMetaNueva,
                    progresoMetaEditado = progresoMetaEditado,
                    onNombreMetaNuevaChange = { nombreMetaNueva = it },
                    onObjetivoMetaNuevaChange = { objetivoMetaNueva = it },
                    onCrearMetaPersonalizada = {
                        viewModel.crearMetaPersonalizada(nombreMetaNueva, objetivoMetaNueva)
                        nombreMetaNueva = ""
                        objetivoMetaNueva = ""
                    },
                    onProgresoMetaChange = { idMeta, texto ->
                        progresoMetaEditado[idMeta] = texto
                    },
                    onGuardarProgresoMeta = { idMeta ->
                        val valor = progresoMetaEditado[idMeta] ?: "0"
                        viewModel.actualizarProgresoMeta(idMeta, valor)
                    },
                    onSolicitarBorrarMeta = { idMeta, nombre ->
                        confirmacionBorrado = ConfirmacionBorrado.Meta(idMeta, nombre)
                    },
                    onSolicitarBorrarPresupuesto = { idCategoria, nombre ->
                        confirmacionBorrado = ConfirmacionBorrado.Presupuesto(idCategoria, nombre)
                    }
                )
            }
        }

        val confirmacionActual = confirmacionBorrado
        if (confirmacionActual != null) {
            AlertDialog(
                onDismissRequest = { confirmacionBorrado = null },
                title = { Text("Confirmar borrado") },
                text = {
                    val nombre = when (confirmacionActual) {
                        is ConfirmacionBorrado.Meta -> confirmacionActual.nombre
                        is ConfirmacionBorrado.Presupuesto -> confirmacionActual.nombre
                    }
                    Text("¿Seguro que quieres borrar \"$nombre\"?")
                },
                confirmButton = {
                    TextButton(onClick = {
                        when (confirmacionActual) {
                            is ConfirmacionBorrado.Meta -> {
                                viewModel.borrarMetaPersonalizada(confirmacionActual.id)
                            }
                            is ConfirmacionBorrado.Presupuesto -> {
                                viewModel.borrarPresupuestoPersonalizado(confirmacionActual.idCategoria)
                            }
                        }
                        confirmacionBorrado = null
                    }) {
                        Text("Si")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmacionBorrado = null }) {
                        Text("No")
                    }
                }
            )
        }
    }
}

@Composable
private fun ContenidoPresupuestos(
    paddingValues: PaddingValues,
    modo: ModoPresupuestos,
    estado: EstadoPresupuestos.Exito,
    valoresEditados: MutableMap<Long, String>,
    metaTexto: String,
    nombrePresupuestoNuevo: String,
    limitePresupuestoNuevo: String,
    nombreMetaNueva: String,
    objetivoMetaNueva: String,
    progresoMetaEditado: MutableMap<Long, String>,
    onMetaTextoChange: (String) -> Unit,
    onGuardarMeta: () -> Unit,
    onNombrePresupuestoNuevoChange: (String) -> Unit,
    onLimitePresupuestoNuevoChange: (String) -> Unit,
    onCrearPresupuesto: () -> Unit,
    onGuardarPresupuesto: (Long) -> Unit,
    onLimiteChange: (Long, String) -> Unit,
    onNombreMetaNuevaChange: (String) -> Unit,
    onObjetivoMetaNuevaChange: (String) -> Unit,
    onCrearMetaPersonalizada: () -> Unit,
    onProgresoMetaChange: (Long, String) -> Unit,
    onGuardarProgresoMeta: (Long) -> Unit,
    onSolicitarBorrarMeta: (Long, String) -> Unit,
    onSolicitarBorrarPresupuesto: (Long, String) -> Unit
) {
    val mostrarMetas = modo == ModoPresupuestos.METAS
    val mostrarPresupuestos = modo == ModoPresupuestos.PRESUPUESTOS

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        if (mostrarMetas) {
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
                TarjetaCrearMetaPersonalizada(
                    nombreMeta = nombreMetaNueva,
                    objetivoMeta = objetivoMetaNueva,
                    onNombreMetaChange = onNombreMetaNuevaChange,
                    onObjetivoMetaChange = onObjetivoMetaNuevaChange,
                    onCrearMeta = onCrearMetaPersonalizada
                )
            }

            if (estado.metasPersonalizadas.isNotEmpty()) {
                item {
                    Text(
                        text = "Tus metas personalizadas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(estado.metasPersonalizadas, key = { it.id }) { meta ->
                    TarjetaMetaPersonalizada(
                        meta = meta,
                        progresoTexto = progresoMetaEditado[meta.id] ?: meta.progresoActual.toString(),
                        onProgresoChange = { onProgresoMetaChange(meta.id, it) },
                        onGuardarProgreso = { onGuardarProgresoMeta(meta.id) },
                        onSolicitarBorrado = { onSolicitarBorrarMeta(meta.id, meta.nombre) }
                    )
                }
            }
        }

        if (mostrarPresupuestos) {
            item {
                TarjetaCrearPresupuesto(
                    nombre = nombrePresupuestoNuevo,
                    limite = limitePresupuestoNuevo,
                    onNombreChange = onNombrePresupuestoNuevoChange,
                    onLimiteChange = onLimitePresupuestoNuevoChange,
                    onCrear = onCrearPresupuesto
                )
            }

            item {
                Text(
                    text = "Tus tarjetas de presupuesto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (estado.presupuestos.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Aún no has creado presupuestos personalizados.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(estado.presupuestos, key = { it.idCategoria }) { item ->
                    val textoLimite = valoresEditados[item.idCategoria] ?: item.limiteMensual.toString()
                    TarjetaPresupuestoCategoria(
                        item = item,
                        limiteTexto = textoLimite,
                        onLimiteChange = { onLimiteChange(item.idCategoria, it) },
                        onGuardar = { onGuardarPresupuesto(item.idCategoria) },
                        onSolicitarBorrado = {
                            onSolicitarBorrarPresupuesto(item.idCategoria, item.nombreCategoria)
                        }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun TarjetaCrearMetaPersonalizada(
    nombreMeta: String,
    objetivoMeta: String,
    onNombreMetaChange: (String) -> Unit,
    onObjetivoMetaChange: (String) -> Unit,
    onCrearMeta: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Crear meta personalizada",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nombreMeta,
                onValueChange = onNombreMetaChange,
                label = { Text("Nombre de la meta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = objetivoMeta,
                onValueChange = onObjetivoMetaChange,
                label = { Text("Objetivo (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onCrearMeta, modifier = Modifier.fillMaxWidth()) {
                Text("Crear meta")
            }
        }
    }
}

@Composable
private fun TarjetaMetaPersonalizada(
    meta: MetaPersonalizadaUi,
    progresoTexto: String,
    onProgresoChange: (String) -> Unit,
    onGuardarProgreso: () -> Unit,
    onSolicitarBorrado: () -> Unit
) {
    val progreso = if (meta.objetivo <= 0.0) 0f else (meta.progresoActual / meta.objetivo).toFloat().coerceIn(0f, 1f)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meta.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onSolicitarBorrado) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Borrar meta"
                    )
                }
            }
            Text(
                text = "Objetivo: ${String.format("%.2f€", meta.objetivo)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Actual: ${String.format("%.2f€", meta.progresoActual)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = progresoTexto,
                    onValueChange = onProgresoChange,
                    label = { Text("Nuevo progreso") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Button(onClick = onGuardarProgreso) {
                    Text("Guardar")
                }
            }
        }
    }
}

@Composable
private fun TarjetaCrearPresupuesto(
    nombre: String,
    limite: String,
    onNombreChange: (String) -> Unit,
    onLimiteChange: (String) -> Unit,
    onCrear: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Crear presupuesto personalizado",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre de la tarjeta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = limite,
                onValueChange = onLimiteChange,
                label = { Text("Límite mensual (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onCrear, modifier = Modifier.fillMaxWidth()) {
                Text("Crear presupuesto")
            }
        }
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
    onGuardar: () -> Unit,
    onSolicitarBorrado: () -> Unit
) {
    val porcentajeUso = if (item.limiteMensual <= 0.0) 0f else (item.gastoMesActual / item.limiteMensual).toFloat()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.nombreCategoria,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onSolicitarBorrado) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Borrar presupuesto"
                    )
                }
            }

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
