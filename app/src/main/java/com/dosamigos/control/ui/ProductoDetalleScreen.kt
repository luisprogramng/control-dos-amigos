package com.dosamigos.control.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dosamigos.control.data.TipoMovimiento
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProductoDetalleScreen(vm: ProductoViewModel, productoId: Int) {
    val context = LocalContext.current
    val productos by vm.productos.collectAsState()
    val producto = productos.find { it.id == productoId }
    val movimientos by vm.movimientosDe(productoId).collectAsState(initial = emptyList())
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    var editandoMinimo by remember { mutableStateOf(false) }
    var stockMinTexto by remember(producto?.stockMinimo) { mutableStateOf(producto?.stockMinimo?.toString() ?: "0") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        if (producto != null) {
            Text(producto.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Categoría: ${producto.categoriaNombre ?: "Sin categoría"}")
            Text("Estado: ${if (producto.activo) "Activo" else "Inactivo"}")
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Stock actual: ${producto.cantidadStock}", fontWeight = FontWeight.Bold)
                Text("Compra: $${producto.precioCompra}  ·  Venta: $${producto.precioVenta}")
            }
            Spacer(Modifier.height(8.dp))

            if (editandoMinimo) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockMinTexto,
                        onValueChange = { stockMinTexto = it },
                        label = { Text("Stock mínimo") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        val nuevo = stockMinTexto.toIntOrNull()
                        if (nuevo != null && nuevo >= 0) {
                            vm.cambiarStockMinimo(producto.id, nuevo)
                            Toast.makeText(context, "Stock mínimo actualizado", Toast.LENGTH_SHORT).show()
                        }
                        editandoMinimo = false
                    }) { Text("Guardar") }
                }
            } else {
                Row(
                    Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Stock mínimo (alerta): ${producto.stockMinimo}")
                    TextButton(onClick = { editandoMinimo = true }) { Text("Editar") }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Text("Historial de movimientos", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (movimientos.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(top = 24.dp)) {
                Text("Este producto aún no tiene movimientos registrados.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(movimientos, key = { it.id }) { mov ->
                    Card {
                        Column(Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(formatoFecha.format(Date(mov.fecha)), style = MaterialTheme.typography.bodySmall)
                                Text(
                                    if (mov.tipo == TipoMovimiento.ENTRADA) "+${mov.cantidad}" else "-${mov.cantidad}",
                                    color = if (mov.tipo == TipoMovimiento.ENTRADA)
                                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (mov.nota.isNotBlank()) Text(mov.nota, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
