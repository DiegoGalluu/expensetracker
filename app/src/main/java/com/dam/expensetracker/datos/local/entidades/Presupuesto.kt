package com.dam.expensetracker.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entidad que representa un presupuesto mensual para una categoría
 */
@Entity(
    tableName = "presupuestos",
    foreignKeys = [
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["idCategoria"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idCategoria")]
)
data class Presupuesto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // ID de la categoría asociada
    val idCategoria: Long,
    
    // Límite mensual del presupuesto
    val limiteMensual: Double
)
