package com.dam.expensetracker.ui.pantallas.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados posibles de la pantalla de login
 */
sealed class EstadoLogin {
    object Inicial : EstadoLogin()
    object Cargando : EstadoLogin()
    data class Autenticado(val emailUsuario: String) : EstadoLogin()
    data class Error(val mensaje: String) : EstadoLogin()
}

/**
 * ViewModel para la pantalla de login
 */
class LoginViewModel : ViewModel() {
    
    private val _estado = MutableStateFlow<EstadoLogin>(EstadoLogin.Inicial)
    val estado: StateFlow<EstadoLogin> = _estado.asStateFlow()
    
    private val _emailUsuario = MutableStateFlow<String?>(null)
    val emailUsuario: StateFlow<String?> = _emailUsuario.asStateFlow()
    
    /**
     * Procesa el resultado del login
     */
    fun onLoginExitoso(email: String) {
        viewModelScope.launch {
            _emailUsuario.value = email
            _estado.value = EstadoLogin.Autenticado(email)
        }
    }
    
    /**
     * Procesa un error en el login
     */
    fun onLoginError(mensajeError: String) {
        viewModelScope.launch {
            _estado.value = EstadoLogin.Error(mensajeError)
        }
    }
    
    /**
     * Inicia el proceso de login
     */
    fun iniciarLogin() {
        viewModelScope.launch {
            _estado.value = EstadoLogin.Cargando
        }
    }
    
    /**
     * Resetea el estado a inicial
     */
    fun resetearEstado() {
        viewModelScope.launch {
            _estado.value = EstadoLogin.Inicial
        }
    }
}
