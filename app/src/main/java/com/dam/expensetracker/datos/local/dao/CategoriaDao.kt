package com.dam.expensetracker.datos.local.dao

import androidx.room.*
import com.dam.expensetracker.datos.local.entidades.Categoria
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de categorías
 */
@Dao
interface CategoriaDao {
    
    @Insert
    suspend fun insertar(categoria: Categoria): Long
    
    @Update
    suspend fun actualizar(categoria: Categoria)
    
    @Delete
    suspend fun borrar(categoria: Categoria)
    
    // Obtener todas las categorías ordenadas alfabéticamente
    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    fun obtenerTodas(): Flow<List<Categoria>>
    
    // Obtener una categoría por su ID
    @Query("SELECT * FROM categorias WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Categoria?
    
    // Buscar categorías por nombre
    @Query("SELECT * FROM categorias WHERE nombre LIKE '%' || :busqueda || '%' ORDER BY nombre ASC")
    fun buscarPorNombre(busqueda: String): Flow<List<Categoria>>
}
