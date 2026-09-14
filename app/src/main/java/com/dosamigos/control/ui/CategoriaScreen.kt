package com.dosamigos.control.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dosamigos.control.data.Categoria

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriaScreen(vm: CategoriaViewModel) {
    val context = LocalContext.current
    val categorias by vm.categorias.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var nombreNuevo by remember { mutableStateOf("") }
    var categoriaAEliminar by remember { mutableStateOf<Categoria?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar categoría")
            }
        }
    ) { padding ->
        if (categorias.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no tienes categorías. Toca el botón + para crear la primera.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categorias, key = { it.id }) { categoria ->
                    Card {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(categoria.nombre, style = MaterialTheme.typography.bodyLarge)
                            IconButton(onClick = { categoriaAEliminar = categoria }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }

    if (categoriaAEliminar != null) {
        AlertDialog(
            onDismissRequest = { categoriaAEliminar = null },
            title = { Text("¿Eliminar categoría?") },
            text = { Text("Los productos de \"${categoriaAEliminar!!.nombre}\" quedarán sin categoría. No se eliminan.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.eliminar(categoriaAEliminar!!)
                    Toast.makeText(context, "Categoría eliminada", Toast.LENGTH_SHORT).show()
                    categoriaAEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { categoriaAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Nueva categoría") },
            text = {
                OutlinedTextField(
                    value = nombreNuevo,
                    onValueChange = { nombreNuevo = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    isError = nombreNuevo.isBlank()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = nombreNuevo.isNotBlank(),
                    onClick = {
                        vm.agregar(nombreNuevo)
                        Toast.makeText(context, "Categoría guardada", Toast.LENGTH_SHORT).show()
                        nombreNuevo = ""
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
