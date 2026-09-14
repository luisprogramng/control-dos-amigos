package com.dosamigos.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoScreen(vm: ProductoViewModel, onAbrirDetalle: (Int) -> Unit) {
    val productos by vm.productos.collectAsState()
    val categorias by vm.categorias.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var productoEntradaRapida by remember { mutableStateOf<Int?>(null) }
    var productoSalidaRapida by remember { mutableStateOf<Int?>(null) }
    var cantidadRapida by remember { mutableStateOf("") }
    var busqueda by remember { mutableStateOf("") }

    var nombre by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var pCompra by remember { mutableStateOf("") }
    var pVenta by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<Int?>(null) }
    var expandido by remember { mutableStateOf(false) }

    val productosFiltrados = remember(productos, busqueda) {
        if (busqueda.isBlank()) productos
        else productos.filter {
            it.nombre.contains(busqueda, ignoreCase = true) ||
            (it.categoriaNombre?.contains(busqueda, ignoreCase = true) == true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 12.dp)) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar producto o categoría") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productosFiltrados, key = { it.id }) { producto ->
                    Card(
                        onClick = { onAbrirDetalle(producto.id) }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    producto.nombre,
                                    fontWeight = FontWeight.Bold,
                                    color = if (producto.activo) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                                        vm.eliminar(
                                            com.dosamigos.control.data.Producto(
                                                id = producto.id, nombre = producto.nombre,
                                                cantidadStock = producto.cantidadStock,
                                                precioCompra = producto.precioCompra,
                                                precioVenta = producto.precioVenta,
                                                categoriaId = producto.categoriaId,
                                                activo = producto.activo
                                            )
                                        )
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                            Text("Categoría: ${producto.categoriaNombre ?: "Sin categoría"}")
                            Text("Stock: ${producto.cantidadStock}")
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
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Nuevo producto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                    OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock inicial") })
                    OutlinedTextField(value = pCompra, onValueChange = { pCompra = it }, label = { Text("Precio compra") })
                    OutlinedTextField(value = pVenta, onValueChange = { pVenta = it }, label = { Text("Precio venta") })

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
                TextButton(onClick = {
                    val stockInt = stock.toIntOrNull() ?: 0
                    val compraD = pCompra.toDoubleOrNull() ?: 0.0
                    val ventaD = pVenta.toDoubleOrNull() ?: 0.0
                    if (nombre.isNotBlank()) {
                        vm.agregar(nombre, stockInt, compraD, ventaD, categoriaSeleccionada)
                        nombre = ""; stock = ""; pCompra = ""; pVenta = ""; categoriaSeleccionada = null
                        mostrarDialogo = false
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }
}
