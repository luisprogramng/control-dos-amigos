package com.dosamigos.control.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpvScreen(vm: IpvViewModel) {
    var enConteo by remember { mutableStateOf(false) }

    if (enConteo) {
        NuevoConteoIpv(vm, onTerminado = { enConteo = false })
    } else {
        HistorialIpv(vm, onNuevo = { enConteo = true })
    }
}

@Composable
private fun HistorialIpv(vm: IpvViewModel, onNuevo: () -> Unit) {
    val historial by vm.historial.collectAsState()
    val context = LocalContext.current
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onNuevo, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Nuevo IPV") })
        }
    ) { padding ->
        if (historial.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no has hecho ningún IPV. Toca \"Nuevo IPV\" para empezar.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historial, key = { it.ipv.id }) { item ->
                    Card {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(formatoFecha.format(Date(item.ipv.fecha)), fontWeight = FontWeight.Bold)
                                IconButton(onClick = {
                                    val nombre = vm.exportarCsv(context, item)
                                    val mensaje = if (nombre != null) "Exportado a Descargas: $nombre" else "No se pudo exportar"
                                    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Exportar a Excel/CSV")
                                }
                            }
                            Text("${item.detalles.size} productos contados")
                            val conDiferencia = item.detalles.count { it.diferencia != 0 }
                            Text("$conDiferencia con diferencia respecto al sistema")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NuevoConteoIpv(vm: IpvViewModel, onTerminado: () -> Unit) {
    val productos by vm.productosActivos.collectAsState()
    val conteos = remember { mutableStateMapOf<Int, String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo IPV") },
                navigationIcon = {
                    IconButton(onClick = onTerminado) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val valores = conteos.mapNotNull { (id, texto) ->
                        texto.toIntOrNull()?.let { id to it }
                    }.toMap()
                    vm.cuadrarIpvAsync(valores) { onTerminado() }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) { Text("Cuadrar y guardar IPV") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(productos, key = { it.id }) { producto ->
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text(producto.nombre, fontWeight = FontWeight.Bold)
                        Text("Stock en sistema: ${producto.cantidadStock}", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = conteos[producto.id] ?: "",
                            onValueChange = { conteos[producto.id] = it },
                            label = { Text("Stock final contado") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
