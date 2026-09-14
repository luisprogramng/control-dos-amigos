package com.dosamigos.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dosamigos.control.data.TipoMovimiento
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProductoDetalleScreen(vm: ProductoViewModel, productoId: Int) {
    val productos by vm.productos.collectAsState()
    val producto = productos.find { it.id == productoId }
    val movimientos by vm.movimientosDe(productoId).collectAsState(initial = emptyList())
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

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
            Spacer(Modifier.height(16.dp))
        }

        Text("Historial de movimientos", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

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
