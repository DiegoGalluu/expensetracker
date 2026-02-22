package com.dam.expensetracker.ui.navegacion

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dam.expensetracker.datos.repositorios.RepositorioDivisas
import com.dam.expensetracker.datos.repositorios.RepositorioFinanzas
import com.dam.expensetracker.ui.pantallas.detalle.DetalleViewModel
import com.dam.expensetracker.ui.pantallas.detalle.PantallaDetalle
import com.dam.expensetracker.ui.pantallas.cuentas.CuentasViewModel
import com.dam.expensetracker.ui.pantallas.cuentas.PantallaCuentas
import com.dam.expensetracker.ui.pantallas.formulario.FormularioViewModel
import com.dam.expensetracker.ui.pantallas.formulario.PantallaFormulario
import com.dam.expensetracker.ui.pantallas.inicio.InicioViewModel
import com.dam.expensetracker.ui.pantallas.inicio.PantallaInicio
import com.dam.expensetracker.ui.pantallas.login.PantallaLogin
import com.dam.expensetracker.ui.pantallas.presupuestos.ModoPresupuestos
import com.dam.expensetracker.ui.pantallas.presupuestos.PantallaPresupuestos
import com.dam.expensetracker.ui.pantallas.presupuestos.PresupuestosViewModel

/**
 * Rutas de navegación de la aplicación
 */
sealed class Ruta(val ruta: String) {
    object Login : Ruta("login")
    object Inicio : Ruta("inicio")
    object Metas : Ruta("metas")
    object Presupuestos : Ruta("presupuestos")
    object Cuentas : Ruta("cuentas")
    object Detalle : Ruta("detalle/{id}") {
        fun crearRuta(id: Long) = "detalle/$id"
    }
    object Formulario : Ruta("formulario?id={id}") {
        fun crearRuta(id: Long? = null) = if (id != null) "formulario?id=$id" else "formulario"
    }
}

/**
 * Grafo de navegación principal
 */
@Composable
fun NavegacionApp(
    repositorioFinanzas: RepositorioFinanzas,
    repositorioDivisas: RepositorioDivisas
) {
    val navController = rememberNavController()
    var emailUsuario by remember { mutableStateOf("") }
    
    NavHost(
        navController = navController,
        startDestination = Ruta.Login.ruta
    ) {
        // Pantalla de Login
        composable(Ruta.Login.ruta) {
            PantallaLogin(
                onLoginExitoso = { email ->
                    emailUsuario = email
                    navController.navigate(Ruta.Inicio.ruta) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                }
            )
        }
        
        // Pantalla de Inicio (Dashboard)
        composable(Ruta.Inicio.ruta) {
            val viewModel: InicioViewModel = viewModel(
                factory = GenericViewModelFactory {
                    InicioViewModel(repositorioFinanzas, repositorioDivisas)
                }
            )
            
            PantallaInicio(
                onNavegarDetalle = { id ->
                    navController.navigate(Ruta.Detalle.crearRuta(id))
                },
                onNavegarFormulario = {
                    navController.navigate(Ruta.Formulario.crearRuta())
                },
                onNavegarMetas = {
                    navController.navigate(Ruta.Metas.ruta)
                },
                onNavegarPresupuestos = {
                    navController.navigate(Ruta.Presupuestos.ruta)
                },
                onNavegarCuentas = {
                    navController.navigate(Ruta.Cuentas.ruta)
                },
                onCerrarSesion = {
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(Ruta.Inicio.ruta) { inclusive = true }
                    }
                },
                emailUsuario = emailUsuario,
                viewModel = viewModel
            )
        }

        composable(Ruta.Metas.ruta) {
            val viewModel: PresupuestosViewModel = viewModel(
                factory = GenericViewModelFactory {
                    PresupuestosViewModel(repositorioFinanzas)
                }
            )

            PantallaPresupuestos(
                onNavegarAtras = { navController.popBackStack() },
                modo = ModoPresupuestos.METAS,
                viewModel = viewModel
            )
        }

        composable(Ruta.Presupuestos.ruta) {
            val viewModel: PresupuestosViewModel = viewModel(
                factory = GenericViewModelFactory {
                    PresupuestosViewModel(repositorioFinanzas)
                }
            )

            PantallaPresupuestos(
                onNavegarAtras = { navController.popBackStack() },
                modo = ModoPresupuestos.PRESUPUESTOS,
                viewModel = viewModel
            )
        }

        composable(Ruta.Cuentas.ruta) {
            val viewModel: CuentasViewModel = viewModel(
                factory = GenericViewModelFactory {
                    CuentasViewModel(repositorioFinanzas)
                }
            )

            PantallaCuentas(
                onNavegarAtras = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
        
        // Pantalla de Detalle
        composable(
            route = Ruta.Detalle.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            val viewModel: DetalleViewModel = viewModel(
                factory = GenericViewModelFactory {
                    DetalleViewModel(repositorioFinanzas)
                }
            )
            
            PantallaDetalle(
                transaccionId = id,
                onNavegarAtras = { navController.popBackStack() },
                onNavegarEditar = { transId ->
                    navController.navigate(Ruta.Formulario.crearRuta(transId))
                },
                viewModel = viewModel
            )
        }
        
        // Pantalla de Formulario (Crear/Editar)
        composable(
            route = Ruta.Formulario.ruta,
            arguments = listOf(navArgument("id") { 
                type = NavType.LongType
                defaultValue = -1L 
            })
        ) { backStackEntry ->
            val idArg = backStackEntry.arguments?.getLong("id")
            val id = if (idArg == -1L) null else idArg
            
            val viewModel: FormularioViewModel = viewModel(
                factory = GenericViewModelFactory {
                    FormularioViewModel(repositorioFinanzas)
                }
            )
            
            PantallaFormulario(
                transaccionId = id,
                onNavegarAtras = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}

/**
 * Factory genérico para ViewModels que requieren parámetros
 */
class GenericViewModelFactory<T : androidx.lifecycle.ViewModel>(
    private val creator: () -> T
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return creator() as T
    }
}
