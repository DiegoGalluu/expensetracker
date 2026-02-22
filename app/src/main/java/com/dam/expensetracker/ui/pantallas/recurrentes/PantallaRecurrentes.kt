package com.dam.expensetracker.ui.pantallas.recurrentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dam.expensetracker.datos.local.entidades.Categoria
import com.dam.expensetracker.datos.local.entidades.Cuenta
import com.dam.expensetracker.datos.local.entidades.RecurringTransaction
import com.dam.expensetracker.ui.tema.AzulSecundario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRecurrentes(
    onNavegarAtras: () -> Unit,
    viewModel: RecurrentesViewModel
) {
    val estado by viewModel.estado.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var esGasto by remember { mutableStateOf(true) }
    var categoriaSeleccionada by remember { mutableStateOf<Categoria?>(null) }
    var cuentaSeleccionada by remember { mutableStateOf<Cuenta?>(null) }
    var frecuencia by remember { mutableStateOf("MENSUAL") }
    var confirmacionBorradoId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurrentes") },
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
            is EstadoRecurrentes.Cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is EstadoRecurrentes.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = estadoActual.mensaje,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            is EstadoRecurrentes.Exito -> {
                if (categoriaSeleccionada == null) categoriaSeleccionada = estadoActual.categorias.firstOrNull()
                if (cuentaSeleccionada == null) cuentaSeleccionada = estadoActual.cuentas.firstOrNull()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Crear recurrente", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = nombre,
                                    onValueChange = { nombre = it },
                                    label = { Text("Nombre") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = cantidad,
                                    onValueChange = { cantidad = it },
                                    label = { Text("Cantidad") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = nota,
                                    onValueChange = { nota = it },
                                    label = { Text("Nota (opcional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Tipo: ")
                                    Text(if (esGasto) "Gasto" else "Ingreso", fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Switch(checked = esGasto, onCheckedChange = { esGasto = it })
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                SelectorFrecuencia(frecuencia = frecuencia, onFrecuenciaChange = { frecuencia = it })

                                Spacer(modifier = Modifier.height(8.dp))
                                SelectorCategoria(
                                    categorias = estadoActual.categorias,
                                    categoriaSeleccionada = categoriaSeleccionada,
                                    onSeleccion = { categoriaSeleccionada = it }
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                SelectorCuenta(
                                    cuentas = estadoActual.cuentas,
                                    cuentaSeleccionada = cuentaSeleccionada,
                                    onSeleccion = { cuentaSeleccionada = it }
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.crearRecurrente(
                                            nombre = nombre,
                                            cantidadTexto = cantidad,
                                            esGasto = esGasto,
                                            idCategoria = categoriaSeleccionada?.id,
                                            idCuenta = cuentaSeleccionada?.id,
                                            frecuencia = frecuencia,
                                            nota = nota
                                        )
                                        nombre = ""
                                        cantidad = ""
                                        nota = ""
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Crear recurrente")
                                }
                            }
                        }
                    }

                    item {
                        Text("Tus recurrentes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    if (estadoActual.recurrentes.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "No hay recurrentes creados todavía.",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(estadoActual.recurrentes, key = { it.id }) { recurrente ->
                            TarjetaRecurrente(
                                recurrente = recurrente,
                                nombreCategoria = estadoActual.categorias.find { it.id == recurrente.idCategoria }?.nombre ?: "Sin categoría",
                                nombreCuenta = estadoActual.cuentas.find { it.id == recurrente.idCuenta }?.nombre ?: "Sin cuenta",
                                onSolicitarBorrado = { confirmacionBorradoId = recurrente.id }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }

                val recurrenteBorrado = estadoActual.recurrentes.find { it.id == confirmacionBorradoId }
                if (recurrenteBorrado != null) {
                    AlertDialog(
                        onDismissRequest = { confirmacionBorradoId = null },
                        title = { Text("Confirmar borrado") },
                        text = { Text("¿Seguro que quieres borrar \"${recurrenteBorrado.nombre}\"?") },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.borrarRecurrente(recurrenteBorrado.id)
                                confirmacionBorradoId = null
                            }) { Text("Si") }
                        },
                        dismissButton = {
                            TextButton(onClick = { confirmacionBorradoId = null }) { Text("No") }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorFrecuencia(
    frecuencia: String,
    onFrecuenciaChange: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    val opciones = listOf("DIARIA", "SEMANAL", "MENSUAL")

    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
        OutlinedTextField(
            value = frecuencia,
            onValueChange = {},
            readOnly = true,
            label = { Text("Frecuencia") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onFrecuenciaChange(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorCategoria(
    categorias: List<Categoria>,
    categoriaSeleccionada: Categoria?,
    onSeleccion: (Categoria) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
        OutlinedTextField(
            value = categoriaSeleccionada?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoría") },
            placeholder = { Text("Selecciona una categoría") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            categorias.forEach { categoria ->
                DropdownMenuItem(
                    text = { Text(categoria.nombre) },
                    onClick = {
                        onSeleccion(categoria)
                        expandido = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorCuenta(
    cuentas: List<Cuenta>,
    cuentaSeleccionada: Cuenta?,
    onSeleccion: (Cuenta) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
        OutlinedTextField(
            value = cuentaSeleccionada?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Cuenta") },
            placeholder = { Text("Selecciona una cuenta") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            cuentas.forEach { cuenta ->
                DropdownMenuItem(
                    text = { Text(cuenta.nombre) },
                    onClick = {
                        onSeleccion(cuenta)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TarjetaRecurrente(
    recurrente: RecurringTransaction,
    nombreCategoria: String,
    nombreCuenta: String,
    onSolicitarBorrado: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AzulSecundario.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recurrente.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onSolicitarBorrado) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Borrar recurrente"
                    )
                }
            }

            Text(text = if (recurrente.esGasto) "Tipo: Gasto" else "Tipo: Ingreso")
            Text(text = "Cantidad: ${String.format("%.2f€", recurrente.cantidad)}")
            Text(text = "Frecuencia: ${recurrente.frecuencia}")
            Text(text = "Categoría: $nombreCategoria")
            Text(text = "Cuenta: $nombreCuenta")
            Text(text = "Activo: ${if (recurrente.activo) "Sí" else "No"}")
        }
    }
}
