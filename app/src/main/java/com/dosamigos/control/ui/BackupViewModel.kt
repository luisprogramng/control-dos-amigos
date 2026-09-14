package com.dosamigos.control.ui

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dosamigos.control.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class BackupViewModel(private val db: AppDatabase) : ViewModel() {

    /** Arma un .json con todas las tablas y lo guarda en Descargas. Devuelve el nombre del archivo o null si falló. */
    fun exportar(context: Context, onListo: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val categorias = db.categoriaDao().getAll().first()
                val productos = db.productoDao().getAllConCategoria().first()
                val movimientos = db.movimientoDao().getAllConProducto().first()
                val historialIpv = db.ipvDao().getHistorial().first()

                val json = JSONObject()

                val arrCategorias = JSONArray()
                categorias.forEach {
                    arrCategorias.put(JSONObject().apply {
                        put("id", it.id)
                        put("nombre", it.nombre)
                    })
                }
                json.put("categorias", arrCategorias)

                val arrProductos = JSONArray()
                productos.forEach {
                    arrProductos.put(JSONObject().apply {
                        put("id", it.id)
                        put("nombre", it.nombre)
                        put("cantidadStock", it.cantidadStock)
                        put("precioCompra", it.precioCompra)
                        put("precioVenta", it.precioVenta)
                        put("categoriaId", it.categoriaId ?: JSONObject.NULL)
                        put("activo", it.activo)
                        put("stockMinimo", it.stockMinimo)
                    })
                }
                json.put("productos", arrProductos)

                val arrMovimientos = JSONArray()
                movimientos.forEach {
                    arrMovimientos.put(JSONObject().apply {
                        put("id", it.id)
                        put("productoId", it.productoId)
                        put("tipo", it.tipo.name)
                        put("cantidad", it.cantidad)
                        put("fecha", it.fecha)
                        put("nota", it.nota)
                    })
                }
                json.put("movimientos", arrMovimientos)

                val arrIpv = JSONArray()
                val arrIpvDetalle = JSONArray()
                historialIpv.forEach { item ->
                    arrIpv.put(JSONObject().apply {
                        put("id", item.ipv.id)
                        put("fecha", item.ipv.fecha)
                    })
                    item.detalles.forEach { d ->
                        arrIpvDetalle.put(JSONObject().apply {
                            put("id", d.id)
                            put("ipvId", d.ipvId)
                            put("productoId", d.productoId ?: JSONObject.NULL)
                            put("productoNombre", d.productoNombre)
                            put("categoriaNombre", d.categoriaNombre ?: JSONObject.NULL)
                            put("stockSistema", d.stockSistema)
                            put("stockContado", d.stockContado)
                            put("diferencia", d.diferencia)
                        })
                    }
                }
                json.put("ipv", arrIpv)
                json.put("ipvDetalle", arrIpvDetalle)

                val formato = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
                val nombreArchivo = "backup_control_dos_amigos_${formato.format(Date())}.json"
                val contenido = json.toString(2)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")
                    }
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    uri?.let {
                        resolver.openOutputStream(it)?.use { out -> out.write(contenido.toByteArray()) }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val downloads = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloads, nombreArchivo)
                    FileOutputStream(file).use { out -> out.write(contenido.toByteArray()) }
                }

                onListo(nombreArchivo)
            } catch (e: Exception) {
                onListo(null)
            }
        }
    }

    /** Borra todo lo actual y restaura desde el .json elegido por el usuario. */
    fun importar(context: Context, uri: Uri, onListo: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val texto = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: return@launch onListo(false)
                val json = JSONObject(texto)

                // Limpiar todo primero (hijos antes que padres)
                db.movimientoDao().deleteAll()
                db.ipvDao().deleteAllDetalles()
                db.ipvDao().deleteAllIpv()
                db.productoDao().deleteAll()
                db.categoriaDao().deleteAll()

                val categorias = json.getJSONArray("categorias")
                for (i in 0 until categorias.length()) {
                    val c = categorias.getJSONObject(i)
                    db.categoriaDao().insert(Categoria(id = c.getInt("id"), nombre = c.getString("nombre")))
                }

                val productos = json.getJSONArray("productos")
                for (i in 0 until productos.length()) {
                    val p = productos.getJSONObject(i)
                    db.productoDao().insert(
                        Producto(
                            id = p.getInt("id"),
                            nombre = p.getString("nombre"),
                            cantidadStock = p.getInt("cantidadStock"),
                            precioCompra = p.getDouble("precioCompra"),
                            precioVenta = p.getDouble("precioVenta"),
                            categoriaId = if (p.isNull("categoriaId")) null else p.getInt("categoriaId"),
                            activo = p.optBoolean("activo", true),
                            stockMinimo = p.optInt("stockMinimo", 0)
                        )
                    )
                }

                val movimientos = json.getJSONArray("movimientos")
                for (i in 0 until movimientos.length()) {
                    val m = movimientos.getJSONObject(i)
                    db.movimientoDao().insert(
                        Movimiento(
                            id = m.getInt("id"),
                            productoId = m.getInt("productoId"),
                            tipo = TipoMovimiento.valueOf(m.getString("tipo")),
                            cantidad = m.getInt("cantidad"),
                            fecha = m.getLong("fecha"),
                            nota = m.optString("nota", "")
                        )
                    )
                }

                val ipvArray = json.getJSONArray("ipv")
                for (i in 0 until ipvArray.length()) {
                    val v = ipvArray.getJSONObject(i)
                    db.ipvDao().insertIpv(Ipv(id = v.getInt("id"), fecha = v.getLong("fecha")))
                }

                val detalleArray = json.getJSONArray("ipvDetalle")
                val listaDetalles = mutableListOf<IpvDetalle>()
                for (i in 0 until detalleArray.length()) {
                    val d = detalleArray.getJSONObject(i)
                    listaDetalles.add(
                        IpvDetalle(
                            id = d.getInt("id"),
                            ipvId = d.getInt("ipvId"),
                            productoId = if (d.isNull("productoId")) null else d.getInt("productoId"),
                            productoNombre = d.getString("productoNombre"),
                            categoriaNombre = if (d.isNull("categoriaNombre")) null else d.getString("categoriaNombre"),
                            stockSistema = d.getInt("stockSistema"),
                            stockContado = d.getInt("stockContado"),
                            diferencia = d.getInt("diferencia")
                        )
                    )
                }
                if (listaDetalles.isNotEmpty()) db.ipvDao().insertDetalles(listaDetalles)

                onListo(true)
            } catch (e: Exception) {
                onListo(false)
            }
        }
    }
}
