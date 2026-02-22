package com.dam.expensetracker.datos.repositorios

import com.dam.expensetracker.datos.local.dao.*
import com.dam.expensetracker.datos.local.entidades.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repositorio que centraliza el acceso a los datos locales (Room)
 * Es el único punto de acceso a los DAOs desde los ViewModels
 */
class RepositorioFinanzas(
    private val transaccionDao: TransaccionDao,
    private val categoriaDao: CategoriaDao,
    private val cuentaDao: CuentaDao,
    private val presupuestoDao: PresupuestoDao,
    private val recurringTransactionDao: RecurringTransactionDao
) {
    
    // ===== TRANSACCIONES =====
    
    fun obtenerTodasTransacciones(): Flow<List<Transaccion>> {
        return transaccionDao.obtenerTodas()
    }
    
    suspend fun obtenerTransaccionPorId(id: Long): Transaccion? {
        return transaccionDao.obtenerPorId(id)
    }
    
    suspend fun insertarTransaccion(transaccion: Transaccion): Long {
        return transaccionDao.insertar(transaccion)
    }
    
    suspend fun actualizarTransaccion(transaccion: Transaccion) {
        transaccionDao.actualizar(transaccion)
    }
    
    suspend fun borrarTransaccion(transaccion: Transaccion) {
        transaccionDao.borrar(transaccion)
    }
    
    fun obtenerTransaccionesPorCategoria(idCategoria: Long): Flow<List<Transaccion>> {
        return transaccionDao.obtenerPorCategoria(idCategoria)
    }
    
    fun obtenerTransaccionesPorCuenta(idCuenta: Long): Flow<List<Transaccion>> {
        return transaccionDao.obtenerPorCuenta(idCuenta)
    }
    
    /**
     * Obtiene las transacciones del mes actual
     */
    fun obtenerTransaccionesMesActual(esGasto: Boolean): Flow<List<Transaccion>> {
        val calendario = Calendar.getInstance()
        
        // Inicio del mes (día 1 a las 00:00:00)
        calendario.set(Calendar.DAY_OF_MONTH, 1)
        calendario.set(Calendar.HOUR_OF_DAY, 0)
        calendario.set(Calendar.MINUTE, 0)
        calendario.set(Calendar.SECOND, 0)
        calendario.set(Calendar.MILLISECOND, 0)
        val inicioMes = calendario.timeInMillis
        
        // Fin del mes (último día a las 23:59:59)
        calendario.add(Calendar.MONTH, 1)
        calendario.add(Calendar.MILLISECOND, -1)
        val finMes = calendario.timeInMillis
        
        return transaccionDao.obtenerPorMes(inicioMes, finMes, esGasto)
    }
    
    /**
     * Calcula el total gastado en una categoría durante el mes actual
     */
    suspend fun obtenerTotalGastadoPorCategoriaEnMesActual(idCategoria: Long): Double {
        val calendario = Calendar.getInstance()
        
        calendario.set(Calendar.DAY_OF_MONTH, 1)
        calendario.set(Calendar.HOUR_OF_DAY, 0)
        calendario.set(Calendar.MINUTE, 0)
        calendario.set(Calendar.SECOND, 0)
        calendario.set(Calendar.MILLISECOND, 0)
        val inicioMes = calendario.timeInMillis
        
        calendario.add(Calendar.MONTH, 1)
        calendario.add(Calendar.MILLISECOND, -1)
        val finMes = calendario.timeInMillis
        
        return transaccionDao.obtenerTotalGastadoPorCategoriaEnMes(idCategoria, inicioMes, finMes) ?: 0.0
    }
    
    // ===== CATEGORÍAS =====
    
    fun obtenerTodasCategorias(): Flow<List<Categoria>> {
        return categoriaDao.obtenerTodas()
    }
    
    suspend fun obtenerCategoriaPorId(id: Long): Categoria? {
        return categoriaDao.obtenerPorId(id)
    }
    
    suspend fun insertarCategoria(categoria: Categoria): Long {
        return categoriaDao.insertar(categoria)
    }
    
    suspend fun actualizarCategoria(categoria: Categoria) {
        categoriaDao.actualizar(categoria)
    }
    
    suspend fun borrarCategoria(categoria: Categoria) {
        categoriaDao.borrar(categoria)
    }
    
    fun buscarCategoriasPorNombre(busqueda: String): Flow<List<Categoria>> {
        return categoriaDao.buscarPorNombre(busqueda)
    }
    
    // ===== CUENTAS =====
    
    fun obtenerTodasCuentas(): Flow<List<Cuenta>> {
        return cuentaDao.obtenerTodas()
    }
    
    suspend fun obtenerCuentaPorId(id: Long): Cuenta? {
        return cuentaDao.obtenerPorId(id)
    }
    
    suspend fun insertarCuenta(cuenta: Cuenta): Long {
        return cuentaDao.insertar(cuenta)
    }
    
    suspend fun actualizarCuenta(cuenta: Cuenta) {
        cuentaDao.actualizar(cuenta)
    }
    
    suspend fun borrarCuenta(cuenta: Cuenta) {
        cuentaDao.borrar(cuenta)
    }
    
    // ===== PRESUPUESTOS =====
    
    fun obtenerTodosPresupuestos(): Flow<List<Presupuesto>> {
        return presupuestoDao.obtenerTodos()
    }
    
    suspend fun obtenerPresupuestoPorCategoria(idCategoria: Long): Presupuesto? {
        return presupuestoDao.obtenerPorCategoria(idCategoria)
    }
    
    suspend fun insertarPresupuesto(presupuesto: Presupuesto): Long {
        return presupuestoDao.insertar(presupuesto)
    }
    
    suspend fun actualizarPresupuesto(presupuesto: Presupuesto) {
        presupuestoDao.actualizar(presupuesto)
    }
    
    suspend fun borrarPresupuesto(presupuesto: Presupuesto) {
        presupuestoDao.borrar(presupuesto)
    }

    // ===== TRANSACCIONES RECURRENTES =====

    fun obtenerTodasTransaccionesRecurrentes(): Flow<List<RecurringTransaction>> {
        return recurringTransactionDao.obtenerTodas()
    }

    suspend fun insertarTransaccionRecurrente(transaccionRecurrente: RecurringTransaction): Long {
        return recurringTransactionDao.insertar(transaccionRecurrente)
    }

    suspend fun actualizarTransaccionRecurrente(transaccionRecurrente: RecurringTransaction) {
        recurringTransactionDao.actualizar(transaccionRecurrente)
    }

    suspend fun borrarTransaccionRecurrente(transaccionRecurrente: RecurringTransaction) {
        recurringTransactionDao.borrar(transaccionRecurrente)
    }

    suspend fun obtenerRecurrentesActivasVencidas(fechaActual: Long): List<RecurringTransaction> {
        return recurringTransactionDao.obtenerActivasVencidas(fechaActual)
    }
}
