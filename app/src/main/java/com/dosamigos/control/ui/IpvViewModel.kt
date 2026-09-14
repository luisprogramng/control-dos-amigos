package com.dosamigos.control.ui

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dosamigos.control.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class ItemConteoIpv(
    val producto: ProductoConCategoria,
    val stockContado: String = ""
)

class IpvViewModel(private val db: AppDatabase) : ViewModel() {

    val historial = db.ipvDao().getHistorial()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productosActivos = db.productoDao().getActivosConCategoria()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Guarda el conteo, calcula la diferencia contra el stock del sistema,
     * ajusta el stock de cada producto y registra el movimiento de ajuste.
     * Devuelve el id del IPV creado.
     */
    suspend fun cuadrarIpv(conteos: Map<Int, Int>): Int {
        val productos = productosActivos.value
        val detalles = mutableListOf<IpvDetalle>()

        val ipvId = db.ipvDao().insertIpv(Ipv()).toInt()

        for (producto in productos) {
            val contado = conteos[producto.id] ?: continue
            val diferencia = contado - producto.cantidadStock

            detalles.add(
                IpvDetalle(
                    ipvId = ipvId,
                    productoId = producto.id,
                    productoNombre = producto.nombre,
                    categoriaNombre = producto.categoriaNombre,
                    stockSistema = producto.cantidadStock,
                    stockContado = contado,
                    diferencia = diferencia
                )
            )

            if (diferencia != 0) {
                db.movimientoDao().insert(
                    Movimiento(
                        productoId = producto.id,
                        tipo = if (diferencia > 0) TipoMovimiento.ENTRADA else TipoMovimiento.SALIDA,
                        cantidad = kotlin.math.abs(diferencia),
                        nota = "Ajuste por IPV"
                    )
                )
                db.productoDao().setStock(producto.id, contado)
            }
        }

        db.ipvDao().insertDetalles(detalles)
        return ipvId
    }

    fun cuadrarIpvAsync(conteos: Map<Int, Int>, onListo: (Int) -> Unit) {
        viewModelScope.launch {
            val id = cuadrarIpv(conteos)
            onListo(id)
        }
    }

    /**
     * Exporta un IPV a un archivo .csv (se abre directamente en Excel/Sheets)
     * guardado en la carpeta Descargas del teléfono.
     * Devuelve el nombre del archivo generado, o null si falló.
     */
    fun exportarCsv(context: Context, ipv: IpvConDetalles): String? {
        val formatoArchivo = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
        val nombreArchivo = "IPV_${formatoArchivo.format(Date(ipv.ipv.fecha))}.csv"

        val sb = StringBuilder()
        sb.append("Producto;Categoria;Stock Sistema;Stock Contado;Diferencia\n")
        for (d in ipv.detalles) {
            sb.append("${d.productoNombre};${d.categoriaNombre ?: "Sin categoría"};${d.stockSistema};${d.stockContado};${d.diferencia}\n")
        }

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    resolver.openOutputStream(it)?.use { out ->
                        out.write(sb.toString().toByteArray())
                    }
                }
                nombreArchivo
            } else {
                @Suppress("DEPRECATION")
                val downloads = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloads, nombreArchivo)
                FileOutputStream(file).use { out -> out.write(sb.toString().toByteArray()) }
                nombreArchivo
            }
        } catch (e: Exception) {
            null
        }
    }
}
