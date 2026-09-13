package com.dosamigos.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoScreen(vm: ProductoViewModel) {
    val productos by vm.productos.collectAsState()
    val categorias by vm.categorias.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var pCompra by remember { mutableStateOf("") }
    var pVenta by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<Int?>(null) }
    var expandido by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(productos, key = { it.id }) { producto ->
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(producto.nombre, fontWeight = FontWeight.Bold)
                            IconButton(onClick = {
                                vm.eliminar(
                                    com.dosamigos.control.data.Producto(
                                        id = producto.id, nombre = producto.nombre,
                                        cantidadStock = producto.cantidadStock,
                                        precioCompra = producto.precioCompra,
                                        precioVenta = producto.precioVenta,
                                        categoriaId = producto.categoriaId
                                    )
                                )
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                        Text("Categoría: ${producto.categoriaNombre ?: "Sin categoría"}")
                        Text("Stock: ${producto.cantidadStock}")
                        Text("Compra: $${producto.precioCompra}  ·  Venta: $${producto.precioVenta}")
                    }
                }
            }
        }
    }

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
