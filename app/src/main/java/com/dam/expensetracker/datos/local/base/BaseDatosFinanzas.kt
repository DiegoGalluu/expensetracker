package com.dam.expensetracker.datos.local.base

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dam.expensetracker.datos.local.dao.*
import com.dam.expensetracker.datos.local.entidades.*

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
        private val INSTANCIAS: MutableMap<String, BaseDatosFinanzas> = mutableMapOf()

        fun obtenerBaseDatos(context: Context): BaseDatosFinanzas {
            return obtenerBaseDatos(context, "anon")
        }

        fun obtenerBaseDatos(context: Context, claveUsuario: String): BaseDatosFinanzas {
            val claveNormalizada = claveUsuario.ifBlank { "anon" }
            val nombreBaseDatos = "finanzas_database_$claveNormalizada"

            return INSTANCIAS[nombreBaseDatos] ?: synchronized(this) {
                INSTANCIAS[nombreBaseDatos] ?: Room.databaseBuilder(
                    context.applicationContext,
                    BaseDatosFinanzas::class.java,
                    nombreBaseDatos
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(CallbackInicial())
                    .build()
                    .also { INSTANCIAS[nombreBaseDatos] = it }
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

                db.execSQL("INSERT INTO categorias(nombre, color) VALUES ('Ahorro mensual', '#2E7D32')")
                db.execSQL("INSERT INTO presupuestos(idCategoria, limiteMensual) VALUES (last_insert_rowid(), 500.0)")
                db.execSQL("INSERT INTO cuentas(nombre) VALUES ('Efectivo')")
                db.execSQL("INSERT INTO cuentas(nombre) VALUES ('Banco')")
            }
        }
    }
}
