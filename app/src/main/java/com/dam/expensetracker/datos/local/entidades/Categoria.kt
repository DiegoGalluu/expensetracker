package com.dam.expensetracker.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una categoría para clasificar transacciones
 * Ejemplos: Comida, Transporte, Ocio, Salud, etc.
 */
@Entity(tableName = "categorias")
data class Categoria(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Nombre de la categoría
    val nombre: String,
    
    // Color en formato hexadecimal (opcional, por ejemplo: "#FF5722")
    val color: String = "#757575"
)
