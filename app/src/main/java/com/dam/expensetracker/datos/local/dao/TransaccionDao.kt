package com.dam.expensetracker.datos.local.dao

import androidx.room.*
import com.dam.expensetracker.datos.local.entidades.Transaccion
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de transacciones
 */
@Dao
interface TransaccionDao {
    
    // Insertar una nueva transacción
    @Insert
    suspend fun insertar(transaccion: Transaccion): Long
    
    // Actualizar una transacción existente
    @Update
    suspend fun actualizar(transaccion: Transaccion)
    
    // Borrar una transacción
    @Delete
    suspend fun borrar(transaccion: Transaccion)
    
    // Obtener todas las transacciones ordenadas por fecha (más reciente primero)
    @Query("SELECT * FROM transacciones ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<Transaccion>>
    
    // Obtener una transacción por su ID
    @Query("SELECT * FROM transacciones WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Transaccion?
    
    // Obtener transacciones de una categoría específica
    @Query("SELECT * FROM transacciones WHERE idCategoria = :idCategoria ORDER BY fecha DESC")
    fun obtenerPorCategoria(idCategoria: Long): Flow<List<Transaccion>>
    
    // Obtener transacciones de un mes específico (gasto o ingreso)
    @Query("""
        SELECT * FROM transacciones 
        WHERE fecha >= :inicioMes AND fecha < :finMes 
        AND esGasto = :esGasto
        ORDER BY fecha DESC
    """)
    fun obtenerPorMes(inicioMes: Long, finMes: Long, esGasto: Boolean): Flow<List<Transaccion>>
    
    // Obtener el total gastado en una categoría en un mes
    @Query("""
        SELECT SUM(cantidad) FROM transacciones 
        WHERE idCategoria = :idCategoria 
        AND fecha >= :inicioMes AND fecha < :finMes 
        AND esGasto = 1
    """)
    suspend fun obtenerTotalGastadoPorCategoriaEnMes(
        idCategoria: Long, 
        inicioMes: Long, 
        finMes: Long
    ): Double?
    
    // Obtener todas las transacciones de una cuenta
    @Query("SELECT * FROM transacciones WHERE idCuenta = :idCuenta ORDER BY fecha DESC")
    fun obtenerPorCuenta(idCuenta: Long): Flow<List<Transaccion>>
}
