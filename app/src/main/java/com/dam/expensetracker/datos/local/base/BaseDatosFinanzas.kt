package com.dam.expensetracker.datos.local.base

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
        Presupuesto::class,
        RecurringTransaction::class
    ],
    version = 2,
    exportSchema = false
)
abstract class BaseDatosFinanzas : RoomDatabase() {
    
    // DAOs abstractos
    abstract fun transaccionDao(): TransaccionDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun cuentaDao(): CuentaDao
    abstract fun presupuestoDao(): PresupuestoDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    
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
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(CallbackInicial())
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `transacciones_recurrentes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `nombre` TEXT NOT NULL,
                        `cantidad` REAL NOT NULL,
                        `esGasto` INTEGER NOT NULL,
                        `idCategoria` INTEGER NOT NULL,
                        `idCuenta` INTEGER NOT NULL,
                        `frecuencia` TEXT NOT NULL,
                        `fechaProximaEjecucion` INTEGER NOT NULL,
                        `activo` INTEGER NOT NULL,
                        `ultimaEjecucion` INTEGER,
                        `nota` TEXT NOT NULL,
                        FOREIGN KEY(`idCategoria`) REFERENCES `categorias`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`idCuenta`) REFERENCES `cuentas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transacciones_recurrentes_idCategoria` ON `transacciones_recurrentes` (`idCategoria`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transacciones_recurrentes_idCuenta` ON `transacciones_recurrentes` (`idCuenta`)")
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
            
            // Única categoría predeterminada
            val categorias = listOf(
                Categoria(nombre = "Ahorro mensual", color = "#2E7D32")
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
