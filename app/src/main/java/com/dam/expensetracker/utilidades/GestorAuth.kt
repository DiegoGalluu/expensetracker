package com.dam.expensetracker.utilidades

import android.app.Activity
import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.CredentialsManager
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials

/**
 * Gestor de autenticación con Auth0
 * 
 * IMPORTANTE: Los valores de DOMINIO y CLIENT_ID son de ejemplo.
 * Debes reemplazarlos con tus propios valores de Auth0:
 * 1. Crea una cuenta en https://auth0.com/
 * 2. Crea una aplicación de tipo "Native"
 * 3. Copia el Domain y Client ID en estas constantes
 * 4. Configura las Callback URLs y Logout URLs en Auth0:
 *    - Callback: com.dam.expensetracker://tu-dominio.auth0.com/android/com.dam.expensetracker/callback
 *    - Logout: com.dam.expensetracker://tu-dominio.auth0.com/android/com.dam.expensetracker/callback
 */
class GestorAuth(private val context: Context) {
    
    // TODO: Reemplazar con tus valores reales de Auth0
    private val DOMINIO = "dev-rvguh8doxbbsj6ox.us.auth0.com"  // Ejemplo: "miapp.eu.auth0.com"
    private val CLIENT_ID = "tiKqPdxJmgjZQkgYVMcl6Kvp5r3Cyu2x"      // Ejemplo: "abcd1234..."
    
    private val cuenta = Auth0(CLIENT_ID, DOMINIO)
    private val SCHEME = "com.dam.expensetracker"
    private var credencialesActuales: Credentials? = null
    private val preferencias = context.getSharedPreferences("auth_session", Context.MODE_PRIVATE)
    private val gestorCredenciales = CredentialsManager(
        AuthenticationAPIClient(cuenta),
        SharedPreferencesStorage(context)
    )
    
    /**
     * Inicia el flujo de login con Auth0
     * Abre el navegador para que el usuario se autentique
     */
    fun iniciarSesion(
        onExito: (String) -> Unit,  // Devuelve el email del usuario
        onError: (String) -> Unit
    ) {
        val activity = context as? Activity
        if (activity == null) {
            onError("No se pudo iniciar Auth0 desde este contexto")
            return
        }

        WebAuthProvider.login(cuenta)
            .withScheme(SCHEME)
            .start(activity, object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(credentials: Credentials) {
                    // Guardamos las credenciales
                    credencialesActuales = credentials
                    gestorCredenciales.saveCredentials(credentials)
                    
                    // Obtenemos información del usuario
                    val client = AuthenticationAPIClient(cuenta)
                    client.userInfo(credentials.accessToken)
                        .start(object : Callback<com.auth0.android.result.UserProfile, AuthenticationException> {
                            override fun onSuccess(profile: com.auth0.android.result.UserProfile) {
                                // Devolvemos el email del usuario
                                val email = profile.email ?: "usuario@ejemplo.com"
                                preferencias.edit().putString("email_usuario", email).apply()
                                onExito(email)
                            }
                            
                            override fun onFailure(error: AuthenticationException) {
                                onError("Error al obtener información del usuario")
                            }
                        })
                }
                
                override fun onFailure(error: AuthenticationException) {
                    onError("Error en el login: ${error.message}")
                }
            })
    }
    
    /**
     * Cierra la sesión del usuario
     */
    fun cerrarSesion(
        onExito: () -> Unit,
        onError: (String) -> Unit
    ) {
        val activity = context as? Activity
        if (activity == null) {
            onError("No se pudo cerrar sesión desde este contexto")
            return
        }

        WebAuthProvider.logout(cuenta)
            .withScheme(SCHEME)
            .start(activity, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(payload: Void?) {
                    credencialesActuales = null
                    gestorCredenciales.clearCredentials()
                    preferencias.edit().remove("email_usuario").apply()
                    onExito()
                }
                
                override fun onFailure(error: AuthenticationException) {
                    onError("Error al cerrar sesión: ${error.message}")
                }
            })
    }
    
    /**
     * Verifica si el usuario está autenticado
     */
    fun estaAutenticado(): Boolean {
        return credencialesActuales != null || gestorCredenciales.hasValidCredentials()
    }
    
    /**
     * Obtiene el token de acceso actual
     */
    fun obtenerToken(): String? {
        return credencialesActuales?.accessToken
    }

    fun recuperarSesionGuardada(
        onExito: (String) -> Unit,
        onNoSesion: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!gestorCredenciales.hasValidCredentials()) {
            onNoSesion()
            return
        }

        gestorCredenciales.getCredentials(object : Callback<Credentials, CredentialsManagerException> {
            override fun onSuccess(result: Credentials) {
                credencialesActuales = result

                val emailGuardado = preferencias.getString("email_usuario", null)
                if (!emailGuardado.isNullOrBlank()) {
                    onExito(emailGuardado)
                    return
                }

                val client = AuthenticationAPIClient(cuenta)
                client.userInfo(result.accessToken)
                    .start(object : Callback<com.auth0.android.result.UserProfile, AuthenticationException> {
                        override fun onSuccess(profile: com.auth0.android.result.UserProfile) {
                            val email = profile.email ?: "usuario@ejemplo.com"
                            preferencias.edit().putString("email_usuario", email).apply()
                            onExito(email)
                        }

                        override fun onFailure(error: AuthenticationException) {
                            onError("No se pudo recuperar el perfil de usuario")
                        }
                    })
            }

            override fun onFailure(error: CredentialsManagerException) {
                onNoSesion()
            }
        })
    }
}
