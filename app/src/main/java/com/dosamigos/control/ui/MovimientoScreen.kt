package com.dosamigos.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dosamigos.control.data.TipoMovimiento
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoScreen(vm: MovimientoViewModel) {
    val movimientos by vm.movimientos.collectAsState()
    val productos by vm.productos.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }

    var productoSeleccionado by remember { mutableStateOf<Int?>(null) }
    var expandido by remember { mutableStateOf(false) }
    var tipo by remember { mutableStateOf(TipoMovimiento.ENTRADA) }
    var cantidad by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }

    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Registrar movimiento")
            }
        }
    ) { padding ->
        if (movimientos.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no hay movimientos registrados.")
            }
        } else {
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(movimientos, key = { it.id }) { mov ->
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(mov.productoNombre, fontWeight = FontWeight.Bold)
                            Text(
                                if (mov.tipo == TipoMovimiento.ENTRADA) "+${mov.cantidad}" else "-${mov.cantidad}",
                                color = if (mov.tipo == TipoMovimiento.ENTRADA)
                                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(formatoFecha.format(Date(mov.fecha)), style = MaterialTheme.typography.bodySmall)
                        if (mov.nota.isNotBlank()) Text(mov.nota, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Registrar movimiento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
                        OutlinedTextField(
                            value = productos.find { it.id == productoSeleccionado }?.nombre ?: "Selecciona un producto",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Producto") },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                            productos.forEach { p ->
                                DropdownMenuItem(text = { Text(p.nombre) }, onClick = {
                                    productoSeleccionado = p.id; expandido = false
                                })
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = tipo == TipoMovimiento.ENTRADA,
                            onClick = { tipo = TipoMovimiento.ENTRADA },
                            label = { Text("Entrada") }
                        )
                        FilterChip(
                            selected = tipo == TipoMovimiento.SALIDA,
                            onClick = { tipo = TipoMovimiento.SALIDA },
                            label = { Text("Salida") }
                        )
                    }

                    OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad") })
                    OutlinedTextField(value = nota, onValueChange = { nota = it }, label = { Text("Nota (opcional)") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val cant = cantidad.toIntOrNull() ?: 0
                    if (productoSeleccionado != null && cant > 0) {
                        vm.registrar(productoSeleccionado!!, tipo, cant, nota)
                        productoSeleccionado = null; cantidad = ""; nota = ""
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
