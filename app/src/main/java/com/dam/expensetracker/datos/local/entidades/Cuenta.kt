package com.dam.expensetracker.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una cuenta bancaria o de efectivo
 * Ejemplos: Efectivo, Banco Santander, Tarjeta Débito, etc.
 */
@Entity(tableName = "cuentas")
data class Cuenta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Nombre de la cuenta
    val nombre: String
)
