package com.dam.expensetracker.ui.pantallas.exportar

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.expensetracker.datos.local.entidades.Transaccion
import com.dam.expensetracker.datos.repositorios.RepositorioFinanzas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TransaccionExportable(
    val fecha: String,
    val tipo: String,
    val cantidad: String,
    val categoria: String,
    val cuenta: String,
    val nota: String
)

sealed class EstadoExportar {
    object Inicial : EstadoExportar()
    object Exportando : EstadoExportar()
    data class Exito(val mensaje: String) : EstadoExportar()
    data class Error(val mensaje: String) : EstadoExportar()
}

class ExportarViewModel(
    private val repositorioFinanzas: RepositorioFinanzas,
    private val appContext: Context
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoExportar>(EstadoExportar.Inicial)
    val estado: StateFlow<EstadoExportar> = _estado.asStateFlow()

    fun exportarCsv() {
        viewModelScope.launch {
            _estado.value = EstadoExportar.Exportando

            try {
                val filas = obtenerFilasExportables()
                val contenido = construirCsv(filas)
                val nombre = "transacciones_${timestampActual()}.csv"

                val uri = guardarArchivo(
                    fileName = nombre,
                    mimeType = "text/csv",
                    bytes = contenido.toByteArray(Charsets.UTF_8)
                )

                if (uri == null) {
                    _estado.value = EstadoExportar.Error("No se pudo exportar el CSV")
                } else {
                    _estado.value = EstadoExportar.Exito("CSV exportado: $nombre")
                }
            } catch (e: Exception) {
                _estado.value = EstadoExportar.Error("Error exportando CSV: ${e.message}")
            }
        }
    }

    fun exportarPdf() {
        viewModelScope.launch {
            _estado.value = EstadoExportar.Exportando

            try {
                val filas = obtenerFilasExportables()
                val bytes = construirPdf(filas)
                val nombre = "transacciones_${timestampActual()}.pdf"

                val uri = guardarArchivo(
                    fileName = nombre,
                    mimeType = "application/pdf",
                    bytes = bytes
                )

                if (uri == null) {
                    _estado.value = EstadoExportar.Error("No se pudo exportar el PDF")
                } else {
                    _estado.value = EstadoExportar.Exito("PDF exportado: $nombre")
                }
            } catch (e: Exception) {
                _estado.value = EstadoExportar.Error("Error exportando PDF: ${e.message}")
            }
        }
    }

    private suspend fun obtenerFilasExportables(): List<TransaccionExportable> {
        val transacciones = repositorioFinanzas.obtenerTodasTransacciones().first()
        val categorias = repositorioFinanzas.obtenerTodasCategorias().first().associateBy { it.id }
        val cuentas = repositorioFinanzas.obtenerTodasCuentas().first().associateBy { it.id }

        return transacciones.map { transaccion ->
            transaccion.toExportable(
                nombreCategoria = categorias[transaccion.idCategoria]?.nombre ?: "Sin categoría",
                nombreCuenta = cuentas[transaccion.idCuenta]?.nombre ?: "Sin cuenta"
            )
        }
    }

    private fun Transaccion.toExportable(nombreCategoria: String, nombreCuenta: String): TransaccionExportable {
        return TransaccionExportable(
            fecha = formatFecha(this.fecha),
            tipo = if (this.esGasto) "Gasto" else "Ingreso",
            cantidad = String.format(Locale.US, "%.2f", this.cantidad),
            categoria = nombreCategoria,
            cuenta = nombreCuenta,
            nota = this.nota
        )
    }

    private fun construirCsv(filas: List<TransaccionExportable>): String {
        val encabezado = "Fecha,Tipo,Cantidad,Categoria,Cuenta,Nota"
        val cuerpo = filas.joinToString(separator = "\n") { fila ->
            listOf(
                fila.fecha,
                fila.tipo,
                fila.cantidad,
                fila.categoria,
                fila.cuenta,
                fila.nota
            ).joinToString(separator = ",") { valor -> escaparCsv(valor) }
        }
        return if (cuerpo.isBlank()) encabezado else "$encabezado\n$cuerpo"
    }

    private fun escaparCsv(valor: String): String {
        val limpio = valor.replace("\"", "\"\"")
        return "\"$limpio\""
    }

    private fun construirPdf(filas: List<TransaccionExportable>): ByteArray {
        val documento = PdfDocument()
        val tituloPaint = Paint().apply { textSize = 16f; isFakeBoldText = true }
        val textoPaint = Paint().apply { textSize = 10f }

        val pageWidth = 595
        val pageHeight = 842
        val left = 30f
        val topStart = 40f
        val lineHeight = 16f

        var paginaNumero = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, paginaNumero).create()
        var page = documento.startPage(pageInfo)
        var canvas = page.canvas
        var y = topStart

        fun dibujarCabecera() {
            canvas.drawText("ExpenseTracker - Exportación de transacciones", left, y, tituloPaint)
            y += 24f
            canvas.drawText("Fecha | Tipo | Cantidad | Categoría | Cuenta | Nota", left, y, textoPaint)
            y += lineHeight
        }

        dibujarCabecera()

        filas.forEach { fila ->
            if (y > pageHeight - 40f) {
                documento.finishPage(page)
                paginaNumero += 1
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, paginaNumero).create()
                page = documento.startPage(pageInfo)
                canvas = page.canvas
                y = topStart
                dibujarCabecera()
            }

            val notaRecortada = if (fila.nota.length > 35) fila.nota.take(35) + "..." else fila.nota
            val linea = "${fila.fecha} | ${fila.tipo} | ${fila.cantidad}€ | ${fila.categoria} | ${fila.cuenta} | $notaRecortada"
            canvas.drawText(linea, left, y, textoPaint)
            y += lineHeight
        }

        documento.finishPage(page)

        val output = java.io.ByteArrayOutputStream()
        documento.writeTo(output)
        documento.close()
        return output.toByteArray()
    }

    private fun guardarArchivo(fileName: String, mimeType: String, bytes: ByteArray) = runCatching {
        val resolver = appContext.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/ExpenseTracker")
        }

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { stream -> stream.write(bytes) }
        }
        uri
    }.getOrNull()

    private fun formatFecha(timestamp: Long): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
    }

    private fun timestampActual(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }
}
