package com.dam.expensetracker.ui.pantallas.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dam.expensetracker.ui.componentes.BotonPrincipal
import com.dam.expensetracker.utilidades.GestorAuth

/**
 * Pantalla de login con Auth0
 */
@Composable
fun PantallaLogin(
    onLoginExitoso: (String) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val gestorAuth = remember { GestorAuth(context) }
    var comprobandoSesion by remember { mutableStateOf(true) }
    
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(Unit) {
        gestorAuth.recuperarSesionGuardada(
            onExito = { email ->
                comprobandoSesion = false
                viewModel.onLoginExitoso(email)
            },
            onNoSesion = {
                comprobandoSesion = false
            },
            onError = {
                comprobandoSesion = false
            }
        )
    }
    
    // Navegar cuando el login sea exitoso
    LaunchedEffect(estado) {
        val estadoActual = estado
        if (estadoActual is EstadoLogin.Autenticado) {
            onLoginExitoso(estadoActual.emailUsuario)
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo o icono de la app
            // Nota: Aquí iría un logo real, usamos un placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💰",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp)
                )
            }
            
            // Título
            Text(
                text = "ExpenseTracker",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtítulo
            Text(
                text = "Gestiona tus finanzas personales",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Botón de login
            when {
                comprobandoSesion || estado is EstadoLogin.Cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                else -> {
                    BotonPrincipal(
                        texto = "Iniciar sesión con Auth0",
                        onClick = {
                            viewModel.iniciarLogin()
                            
                            gestorAuth.iniciarSesion(
                                onExito = { email ->
                                    viewModel.onLoginExitoso(email)
                                },
                                onError = { error ->
                                    viewModel.onLoginError(error)
                                }
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mensaje de error
            if (estado is EstadoLogin.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = (estado as EstadoLogin.Error).mensaje,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
