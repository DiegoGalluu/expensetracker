package com.dam.expensetracker.datos.local.dao

import androidx.room.*
import com.dam.expensetracker.datos.local.entidades.Presupuesto
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de presupuestos
 */
@Dao
interface PresupuestoDao {
    
    @Insert
    suspend fun insertar(presupuesto: Presupuesto): Long
    
    @Update
    suspend fun actualizar(presupuesto: Presupuesto)
    
    @Delete
    suspend fun borrar(presupuesto: Presupuesto)
    
    // Obtener todos los presupuestos
    @Query("SELECT * FROM presupuestos")
    fun obtenerTodos(): Flow<List<Presupuesto>>
    
    // Obtener presupuesto de una categoría específica
    @Query("SELECT * FROM presupuestos WHERE idCategoria = :idCategoria")
    suspend fun obtenerPorCategoria(idCategoria: Long): Presupuesto?
    
    // Obtener presupuesto por su ID
    @Query("SELECT * FROM presupuestos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Presupuesto?
}
