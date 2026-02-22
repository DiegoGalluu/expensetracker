package com.dam.expensetracker.datos.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dam.expensetracker.datos.local.entidades.RecurringTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {

    @Insert
    suspend fun insertar(transaccionRecurrente: RecurringTransaction): Long

    @Update
    suspend fun actualizar(transaccionRecurrente: RecurringTransaction)

    @Delete
    suspend fun borrar(transaccionRecurrente: RecurringTransaction)

    @Query("SELECT * FROM transacciones_recurrentes ORDER BY fechaProximaEjecucion ASC")
    fun obtenerTodas(): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM transacciones_recurrentes WHERE activo = 1 AND fechaProximaEjecucion <= :fechaActual")
    suspend fun obtenerActivasVencidas(fechaActual: Long): List<RecurringTransaction>

    @Query("SELECT * FROM transacciones_recurrentes WHERE id = :id")
    suspend fun obtenerPorId(id: Long): RecurringTransaction?
}
