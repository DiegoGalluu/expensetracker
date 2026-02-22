package com.dam.expensetracker.ui.pantallas.exportar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaExportar(
    onNavegarAtras: () -> Unit,
    viewModel: ExportarViewModel
) {
    val estado by viewModel.estado.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exportar datos") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Exportación local",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Los archivos se guardarán en Documents/ExpenseTracker",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            val exportando = estado is EstadoExportar.Exportando

            Button(
                onClick = { viewModel.exportarCsv() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !exportando
            ) {
                Text("Exportar CSV")
            }

            Button(
                onClick = { viewModel.exportarPdf() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !exportando
            ) {
                Text("Exportar PDF")
            }

            when (val estadoActual = estado) {
                is EstadoExportar.Inicial -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Selecciona el formato de exportación.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                is EstadoExportar.Exportando -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is EstadoExportar.Exito -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = estadoActual.mensaje,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                is EstadoExportar.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
        }
    }
}
