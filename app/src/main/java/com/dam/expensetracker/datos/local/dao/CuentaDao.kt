package com.dam.expensetracker.datos.local.dao

import androidx.room.*
import com.dam.expensetracker.datos.local.entidades.Cuenta
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de cuentas
 */
@Dao
interface CuentaDao {
    
    @Insert
    suspend fun insertar(cuenta: Cuenta): Long
    
    @Update
    suspend fun actualizar(cuenta: Cuenta)
    
    @Delete
    suspend fun borrar(cuenta: Cuenta)
    
    // Obtener todas las cuentas ordenadas alfabéticamente
    @Query("SELECT * FROM cuentas ORDER BY nombre ASC")
    fun obtenerTodas(): Flow<List<Cuenta>>
    
    // Obtener una cuenta por su ID
    @Query("SELECT * FROM cuentas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Cuenta?
}
