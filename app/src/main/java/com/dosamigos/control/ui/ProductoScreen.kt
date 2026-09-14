package com.dosamigos.control.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoScreen(vm: ProductoViewModel, onAbrirDetalle: (Int) -> Unit) {
    val context = LocalContext.current
    val productos by vm.productos.collectAsState()
    val categorias by vm.categorias.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var productoEntradaRapida by remember { mutableStateOf<Int?>(null) }
    var productoSalidaRapida by remember { mutableStateOf<Int?>(null) }
    var productoAEliminar by remember { mutableStateOf<com.dosamigos.control.data.Producto?>(null) }
    var cantidadRapida by remember { mutableStateOf("") }
    var busqueda by remember { mutableStateOf("") }
    var soloStockBajo by remember { mutableStateOf(false) }
    var ordenPorStock by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var stockMin by remember { mutableStateOf("0") }
    var pCompra by remember { mutableStateOf("") }
    var pVenta by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<Int?>(null) }
    var expandido by remember { mutableStateOf(false) }

    val productosFiltrados = remember(productos, busqueda, soloStockBajo, ordenPorStock) {
        var lista = productos.filter {
            (busqueda.isBlank() || it.nombre.contains(busqueda, ignoreCase = true) ||
                (it.categoriaNombre?.contains(busqueda, ignoreCase = true) == true)) &&
            (!soloStockBajo || it.cantidadStock <= it.stockMinimo)
        }
        lista = if (ordenPorStock) lista.sortedBy { it.cantidadStock } else lista.sortedWith(compareByDescending<com.dosamigos.control.data.ProductoConCategoria> { it.activo }.thenBy { it.nombre })
        lista
    }

    val valorInventario = remember(productos) { productos.sumOf { it.cantidadStock * it.precioVenta } }
    val cantidadStockBajo = remember(productos) { productos.count { it.activo && it.cantidadStock <= it.stockMinimo } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 12.dp)) {

            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Valor del inventario", style = MaterialTheme.typography.bodySmall)
                        Text("$${"%.2f".format(valorInventario)}", fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Stock bajo", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "$cantidadStockBajo producto(s)",
                            fontWeight = FontWeight.Bold,
                            color = if (cantidadStockBajo > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar producto o categoría") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = soloStockBajo, onClick = { soloStockBajo = !soloStockBajo }, label = { Text("Solo stock bajo") })
                FilterChip(selected = ordenPorStock, onClick = { ordenPorStock = !ordenPorStock }, label = { Text("Ordenar por stock") })
            }

            if (productos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aún no tienes productos. Toca el botón + para agregar el primero.")
                }
            } else if (productosFiltrados.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay productos que coincidan con el filtro.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(productosFiltrados, key = { it.id }) { producto ->
                        val stockBajo = producto.cantidadStock <= producto.stockMinimo
                        Card(onClick = { onAbrirDetalle(producto.id) }) {
                            Column(Modifier.padding(16.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (stockBajo && producto.activo) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = "Stock bajo",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                        }
                                        Text(
                                            producto.nombre,
                                            fontWeight = FontWeight.Bold,
                                            color = if (producto.activo) MaterialTheme.colorScheme.onSurface
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row {
                                        IconButton(onClick = {
                                            productoEntradaRapida = producto.id
                                            cantidadRapida = ""
                                        }) {
                                            Icon(Icons.Default.AddCircle, contentDescription = "Entrada rápida")
                                        }
                                        IconButton(onClick = {
                                            productoSalidaRapida = producto.id
                                            cantidadRapida = ""
                                        }) {
                                            Icon(Icons.Default.RemoveCircle, contentDescription = "Salida rápida")
                                        }
                                        IconButton(onClick = {
                                            productoAEliminar = com.dosamigos.control.data.Producto(
                                                id = producto.id, nombre = producto.nombre,
                                                cantidadStock = producto.cantidadStock,
                                                precioCompra = producto.precioCompra,
                                                precioVenta = producto.precioVenta,
                                                categoriaId = producto.categoriaId,
                                                activo = producto.activo,
                                                stockMinimo = producto.stockMinimo
                                            )
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                        }
                                    }
                                }
                                Text("Categoría: ${producto.categoriaNombre ?: "Sin categoría"}")
                                Text(
                                    "Stock: ${producto.cantidadStock} (mínimo: ${producto.stockMinimo})",
                                    color = if (stockBajo && producto.activo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                                Text("Compra: $${producto.precioCompra}  ·  Venta: $${producto.precioVenta}")

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(if (producto.activo) "Activo" else "Inactivo", style = MaterialTheme.typography.bodySmall)
                                    Switch(
                                        checked = producto.activo,
                                        onCheckedChange = { vm.cambiarActivo(producto.id, it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmar eliminación
    if (productoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("¿Eliminar producto?") },
            text = { Text("Se eliminará \"${productoAEliminar!!.nombre}\" junto con su historial de movimientos. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.eliminar(productoAEliminar!!)
                    Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    productoAEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de entrada rápida
    if (productoEntradaRapida != null) {
        AlertDialog(
            onDismissRequest = { productoEntradaRapida = null },
            title = { Text("Entrada rápida de stock") },
            text = {
                OutlinedTextField(
                    value = cantidadRapida,
                    onValueChange = { cantidadRapida = it },
                    label = { Text("Cantidad a agregar") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val cant = cantidadRapida.toIntOrNull() ?: 0
                    if (cant > 0) {
                        vm.entradaRapida(productoEntradaRapida!!, cant)
                        Toast.makeText(context, "Entrada registrada", Toast.LENGTH_SHORT).show()
                    }
                    productoEntradaRapida = null
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { productoEntradaRapida = null }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de salida rápida
    if (productoSalidaRapida != null) {
        AlertDialog(
            onDismissRequest = { productoSalidaRapida = null },
            title = { Text("Salida rápida de stock") },
            text = {
                OutlinedTextField(
                    value = cantidadRapida,
                    onValueChange = { cantidadRapida = it },
                    label = { Text("Cantidad a restar") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val cant = cantidadRapida.toIntOrNull() ?: 0
                    if (cant > 0) {
                        vm.salidaRapida(productoSalidaRapida!!, cant)
                        Toast.makeText(context, "Salida registrada", Toast.LENGTH_SHORT).show()
                    }
                    productoSalidaRapida = null
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { productoSalidaRapida = null }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de nuevo producto
    if (mostrarDialogo) {
        val stockD = stock.toIntOrNull()
        val compraD = pCompra.toDoubleOrNull()
        val ventaD = pVenta.toDoubleOrNull()
        val stockMinD = stockMin.toIntOrNull()
        val esValido = nombre.isNotBlank() && stockD != null && stockD >= 0 &&
                compraD != null && compraD >= 0 && ventaD != null && ventaD >= 0 &&
                stockMinD != null && stockMinD >= 0

        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Nuevo producto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") },
                        isError = nombre.isBlank()
                    )
                    OutlinedTextField(
                        value = stock, onValueChange = { stock = it }, label = { Text("Stock inicial") },
                        isError = stockD == null || stockD < 0
                    )
                    OutlinedTextField(
                        value = stockMin, onValueChange = { stockMin = it }, label = { Text("Stock mínimo (alerta)") },
                        isError = stockMinD == null || stockMinD < 0
                    )
                    OutlinedTextField(
                        value = pCompra, onValueChange = { pCompra = it }, label = { Text("Precio compra") },
                        isError = compraD == null || compraD < 0
                    )
                    OutlinedTextField(
                        value = pVenta, onValueChange = { pVenta = it }, label = { Text("Precio venta") },
                        isError = ventaD == null || ventaD < 0
                    )

                    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
                        OutlinedTextField(
                            value = categorias.find { it.id == categoriaSeleccionada }?.nombre ?: "Sin categoría",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría") },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                            DropdownMenuItem(text = { Text("Sin categoría") }, onClick = {
                                categoriaSeleccionada = null; expandido = false
                            })
                            categorias.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat.nombre) }, onClick = {
                                    categoriaSeleccionada = cat.id; expandido = false
                                })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = esValido,
                    onClick = {
                        vm.agregar(nombre, stockD!!, compraD!!, ventaD!!, categoriaSeleccionada, stockMinD!!)
                        Toast.makeText(context, "Producto guardado", Toast.LENGTH_SHORT).show()
                        nombre = ""; stock = ""; stockMin = "0"; pCompra = ""; pVenta = ""; categoriaSeleccionada = null
                        mostrarDialogo = false
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }
}
