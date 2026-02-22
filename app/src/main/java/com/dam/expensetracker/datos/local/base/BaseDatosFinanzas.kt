package com.dam.expensetracker.datos.local.base

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dam.expensetracker.datos.local.dao.*
import com.dam.expensetracker.datos.local.entidades.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos Room que contiene todas las tablas de la app
 */
@Database(
    entities = [
        Transaccion::class,
        Categoria::class,
        Cuenta::class,
        Presupuesto::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BaseDatosFinanzas : RoomDatabase() {
    
    // DAOs abstractos
    abstract fun transaccionDao(): TransaccionDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun cuentaDao(): CuentaDao
    abstract fun presupuestoDao(): PresupuestoDao
    
    companion object {
        @Volatile
        private var INSTANCIA: BaseDatosFinanzas? = null
        
        fun obtenerBaseDatos(context: Context): BaseDatosFinanzas {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    BaseDatosFinanzas::class.java,
                    "finanzas_database"
                )
                    .addCallback(CallbackInicial())
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }
        
        /**
         * Callback para poblar la base de datos con datos iniciales
         */
        private class CallbackInicial : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                
                // Insertar datos iniciales en un hilo de fondo
                INSTANCIA?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        poblarDatosIniciales(database)
                    }
                }
            }
        }
        
        /**
         * Método para insertar categorías, cuentas y presupuestos por defecto
         */
        private suspend fun poblarDatosIniciales(database: BaseDatosFinanzas) {
            val categoriaDao = database.categoriaDao()
            val cuentaDao = database.cuentaDao()
            val presupuestoDao = database.presupuestoDao()
            
            // Categorías por defecto con colores
            val categorias = listOf(
                Categoria(nombre = "Comida", color = "#FF5722"),
                Categoria(nombre = "Transporte", color = "#2196F3"),
                Categoria(nombre = "Ocio", color = "#4CAF50"),
                Categoria(nombre = "Salud", color = "#F44336"),
                Categoria(nombre = "Vivienda", color = "#9C27B0"),
                Categoria(nombre = "Educación", color = "#FF9800"),
                Categoria(nombre = "Otros", color = "#757575")
            )
            
            categorias.forEach { categoria ->
                val id = categoriaDao.insertar(categoria)
                
                // Crear presupuesto por defecto para cada categoría (500€)
                presupuestoDao.insertar(
                    Presupuesto(
                        idCategoria = id,
                        limiteMensual = 500.0
                    )
                )
            }
            
            // Cuentas por defecto
            val cuentas = listOf(
                Cuenta(nombre = "Efectivo"),
                Cuenta(nombre = "Banco")
            )
            
            cuentas.forEach { cuenta ->
                cuentaDao.insertar(cuenta)
            }
        }
    }
}
