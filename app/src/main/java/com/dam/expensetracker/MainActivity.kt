package com.dam.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dam.expensetracker.datos.local.base.BaseDatosFinanzas
import com.dam.expensetracker.datos.remoto.api.ApiBancariaSimulada
import com.dam.expensetracker.datos.remoto.api.ApiDivisas
import com.dam.expensetracker.datos.repositorios.RepositorioBancarioSimulado
import com.dam.expensetracker.datos.repositorios.RepositorioDivisas
import com.dam.expensetracker.datos.repositorios.RepositorioFinanzas
import com.dam.expensetracker.ui.navegacion.NavegacionApp
import com.dam.expensetracker.ui.tema.TemaExpenseTracker
import com.dam.expensetracker.utilidades.Constantes
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Actividad principal de la aplicación
 */
class MainActivity : ComponentActivity() {

    // Repositorios
    private lateinit var repositorioDivisas: RepositorioDivisas
    private lateinit var repositorioBancarioSimulado: RepositorioBancarioSimulado

    private fun normalizarClaveUsuario(email: String): String {
        val base = email.trim().lowercase().ifBlank { "anon" }
        return buildString {
            base.forEach { caracter ->
                append(
                    when {
                        caracter.isLetterOrDigit() -> caracter
                        else -> '_'
                    }
                )
            }
        }.take(80)
    }

    private fun crearRepositorioFinanzasPorUsuario(email: String): RepositorioFinanzas {
        val claveUsuario = normalizarClaveUsuario(email)
        val baseDatos = BaseDatosFinanzas.obtenerBaseDatos(applicationContext, claveUsuario)
        return RepositorioFinanzas(
            transaccionDao = baseDatos.transaccionDao(),
            categoriaDao = baseDatos.categoriaDao(),
            cuentaDao = baseDatos.cuentaDao(),
            presupuestoDao = baseDatos.presupuestoDao(),
            recurringTransactionDao = baseDatos.recurringTransactionDao()
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar API de divisas con Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(Constantes.BASE_URL_API_DIVISAS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        val apiDivisas = retrofit.create(ApiDivisas::class.java)
        
        // Inicializar repositorio de divisas
        repositorioDivisas = RepositorioDivisas(apiDivisas)

        // Inicializar API bancaria simulada con Retrofit
        val retrofitBancario = Retrofit.Builder()
            .baseUrl(Constantes.BASE_URL_API_BANCARIA_SIMULADA)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiBancariaSimulada = retrofitBancario.create(ApiBancariaSimulada::class.java)

        // Inicializar repositorio bancario simulado
        repositorioBancarioSimulado = RepositorioBancarioSimulado(apiBancariaSimulada)
        
        setContent {
            TemaExpenseTracker {
                NavegacionApp(
                    crearRepositorioFinanzas = { email -> crearRepositorioFinanzasPorUsuario(email) },
                    repositorioDivisas = repositorioDivisas,
                    repositorioBancarioSimulado = repositorioBancarioSimulado
                )
            }
        }
    }
}
