package com.dam.expensetracker.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entidad que representa una transacción financiera (gasto o ingreso)
 */
@Entity(
    tableName = "transacciones",
    foreignKeys = [
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["idCategoria"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Cuenta::class,
            parentColumns = ["id"],
            childColumns = ["idCuenta"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idCategoria"), Index("idCuenta")]
)
data class Transaccion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Cantidad de dinero (siempre positiva)
    val cantidad: Double,
    
    // Fecha en formato timestamp (milisegundos)
    val fecha: Long = System.currentTimeMillis(),
    
    // True si es gasto, False si es ingreso
    val esGasto: Boolean,
    
    // ID de la categoría asociada
    val idCategoria: Long,
    
    // ID de la cuenta asociada
    val idCuenta: Long,
    
    // Nota o descripción opcional
    val nota: String = ""
)
