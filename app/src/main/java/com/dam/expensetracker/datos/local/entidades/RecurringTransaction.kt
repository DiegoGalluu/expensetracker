package com.dam.expensetracker.datos.local.entidades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transacciones_recurrentes",
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
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val cantidad: Double,
    val esGasto: Boolean,
    val idCategoria: Long,
    val idCuenta: Long,
    val frecuencia: String,
    val fechaProximaEjecucion: Long,
    val activo: Boolean = true,
    val ultimaEjecucion: Long? = null,
    val nota: String = ""
)
