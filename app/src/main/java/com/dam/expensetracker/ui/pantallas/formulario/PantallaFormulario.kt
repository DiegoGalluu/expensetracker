package com.dam.expensetracker.ui.pantallas.formulario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dam.expensetracker.datos.local.entidades.Categoria
import com.dam.expensetracker.datos.local.entidades.Cuenta
import com.dam.expensetracker.ui.componentes.BotonPrincipal

/**
 * Pantalla de formulario para crear o editar una transacción
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormulario(
    transaccionId: Long? = null,
    onNavegarAtras: () -> Unit,
    viewModel: FormularioViewModel
) {
    val estado by viewModel.estado.collectAsState()
    val cantidad by viewModel.cantidad.collectAsState()
    val nota by viewModel.nota.collectAsState()
    val esGasto by viewModel.esGasto.collectAsState()
    val categoriaSeleccionada by viewModel.categoriaSeleccionada.collectAsState()
    val cuentaSeleccionada by viewModel.cuentaSeleccionada.collectAsState()
    var mostrarModalNuevaCategoria by remember { mutableStateOf(false) }
    var nombreNuevaCategoria by remember { mutableStateOf("") }
    var errorNuevaCategoria by remember { mutableStateOf<String?>(null) }
    
    // Cargar datos al crear la pantalla
    LaunchedEffect(transaccionId) {
        viewModel.cargarDatos(transaccionId)
    }
    
    // Navegar atrás al guardar
    LaunchedEffect(estado) {
        if (estado is EstadoFormulario.Guardado) {
            onNavegarAtras()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (transaccionId == null) "Nueva transacción" else "Editar transacción") 
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
            is EstadoFormulario.Cargando, EstadoFormulario.Inicial -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is EstadoFormulario.CargandoDatos -> {
                FormularioContenido(
                    cantidad = cantidad,
                    nota = nota,
                    esGasto = esGasto,
                    categoriaSeleccionada = categoriaSeleccionada,
                    cuentaSeleccionada = cuentaSeleccionada,
                    categorias = estadoActual.categorias,
                    cuentas = estadoActual.cuentas,
                    onCantidadChange = { viewModel.actualizarCantidad(it) },
                    onNotaChange = { viewModel.actualizarNota(it) },
                    onTipoChange = { viewModel.cambiarTipo(it) },
                    onCategoriaChange = { viewModel.seleccionarCategoria(it) },
                    onCrearNuevaCategoria = {
                        mostrarModalNuevaCategoria = true
                        nombreNuevaCategoria = ""
                        errorNuevaCategoria = null
                    },
                    onCuentaChange = { viewModel.seleccionarCuenta(it) },
                    onGuardar = { viewModel.guardarTransaccion() },
                    paddingValues = paddingValues,
                    mensajeError = null
                )
            }

            is EstadoFormulario.CargandoDatosConError -> {
                FormularioContenido(
                    cantidad = cantidad,
                    nota = nota,
                    esGasto = esGasto,
                    categoriaSeleccionada = categoriaSeleccionada,
                    cuentaSeleccionada = cuentaSeleccionada,
                    categorias = estadoActual.categorias,
                    cuentas = estadoActual.cuentas,
                    onCantidadChange = { viewModel.actualizarCantidad(it) },
                    onNotaChange = { viewModel.actualizarNota(it) },
                    onTipoChange = { viewModel.cambiarTipo(it) },
                    onCategoriaChange = { viewModel.seleccionarCategoria(it) },
                    onCrearNuevaCategoria = {
                        mostrarModalNuevaCategoria = true
                        nombreNuevaCategoria = ""
                        errorNuevaCategoria = null
                    },
                    onCuentaChange = { viewModel.seleccionarCuenta(it) },
                    onGuardar = { viewModel.guardarTransaccion() },
                    paddingValues = paddingValues,
                    mensajeError = estadoActual.mensaje
                )
            }
            
            is EstadoFormulario.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = estadoActual.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            is EstadoFormulario.Guardado -> {
                // Manejado con LaunchedEffect
            }
        }
    }

    if (mostrarModalNuevaCategoria) {
        AlertDialog(
            onDismissRequest = {
                mostrarModalNuevaCategoria = false
                errorNuevaCategoria = null
            },
            title = { Text("Nueva categoría") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nombreNuevaCategoria,
                        onValueChange = {
                            nombreNuevaCategoria = it
                            errorNuevaCategoria = null
                        },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorNuevaCategoria != null
                    )

                    if (!errorNuevaCategoria.isNullOrBlank()) {
                        Text(
                            text = errorNuevaCategoria!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.crearCategoriaDesdeSelector(nombreNuevaCategoria) { error ->
                            if (error == null) {
                                mostrarModalNuevaCategoria = false
                                nombreNuevaCategoria = ""
                                errorNuevaCategoria = null
                            } else {
                                errorNuevaCategoria = error
                            }
                        }
                    }
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarModalNuevaCategoria = false
                        errorNuevaCategoria = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FormularioContenido(
    cantidad: String,
    nota: String,
    esGasto: Boolean,
    categoriaSeleccionada: Categoria?,
    cuentaSeleccionada: Cuenta?,
    categorias: List<Categoria>,
    cuentas: List<Cuenta>,
    onCantidadChange: (String) -> Unit,
    onNotaChange: (String) -> Unit,
    onTipoChange: (Boolean) -> Unit,
    onCategoriaChange: (Categoria) -> Unit,
    onCrearNuevaCategoria: () -> Unit,
    onCuentaChange: (Cuenta) -> Unit,
    onGuardar: () -> Unit,
    paddingValues: PaddingValues,
    mensajeError: String?
) {
    var expandidoCategorias by remember { mutableStateOf(false) }
    var expandidoCuentas by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selector de tipo (Gasto / Ingreso)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tipo de transacción",
                    style = MaterialTheme.typography.labelLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = esGasto,
                        onClick = { onTipoChange(true) },
                        label = { Text("Gasto") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    FilterChip(
                        selected = !esGasto,
                        onClick = { onTipoChange(false) },
                        label = { Text("Ingreso") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Campo de cantidad
        OutlinedTextField(
            value = cantidad,
            onValueChange = onCantidadChange,
            label = { Text("Cantidad") },
            suffix = { Text("€") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Selector de categoría
        ExposedDropdownMenuBox(
            expanded = expandidoCategorias,
            onExpandedChange = { expandidoCategorias = it }
        ) {
            OutlinedTextField(
                value = categoriaSeleccionada?.nombre ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría") },
                placeholder = { Text("Selecciona una categoría") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoCategorias) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
            )
            
            ExposedDropdownMenu(
                expanded = expandidoCategorias,
                onDismissRequest = { expandidoCategorias = false }
            ) {
                categorias.forEach { categoria ->
                    DropdownMenuItem(
                        text = { Text(categoria.nombre) },
                        onClick = {
                            onCategoriaChange(categoria)
                            expandidoCategorias = false
                        }
                    )
                }

                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("+ Crear nueva categoría") },
                    onClick = {
                        expandidoCategorias = false
                        onCrearNuevaCategoria()
                    }
                )
            }
        }
        
        // Selector de cuenta
        ExposedDropdownMenuBox(
            expanded = expandidoCuentas,
            onExpandedChange = { expandidoCuentas = it }
        ) {
            OutlinedTextField(
                value = cuentaSeleccionada?.nombre ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Cuenta") },
                placeholder = { Text("Selecciona una cuenta") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoCuentas) },
                modifier = Modifier
                    .fillMaxWidth()
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
            )
            
            ExposedDropdownMenu(
                expanded = expandidoCuentas,
                onDismissRequest = { expandidoCuentas = false }
            ) {
                cuentas.forEach { cuenta ->
                    DropdownMenuItem(
                        text = { Text(cuenta.nombre) },
                        onClick = {
                            onCuentaChange(cuenta)
                            expandidoCuentas = false
                        }
                    )
                }
            }
        }
        
        // Campo de nota
        OutlinedTextField(
            value = nota,
            onValueChange = onNotaChange,
            label = { Text("Nota (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
        
        // Mensaje de error
        if (mensajeError != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = mensajeError,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Botón de guardar
        BotonPrincipal(
            texto = "Guardar",
            onClick = onGuardar
        )
    }
}
