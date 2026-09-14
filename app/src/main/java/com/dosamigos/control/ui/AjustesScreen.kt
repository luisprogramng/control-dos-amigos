package com.dosamigos.control.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AjustesScreen(vm: BackupViewModel) {
    val context = LocalContext.current
    var exportando by remember { mutableStateOf(false) }
    var importando by remember { mutableStateOf(false) }
    var mostrarConfirmacionImportar by remember { mutableStateOf<android.net.Uri?>(null) }

    val selectorArchivo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) mostrarConfirmacionImportar = uri
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Copia de seguridad", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Guarda toda la información de la app (categorías, productos, movimientos e IPV) " +
            "en un archivo en Descargas. Si desinstalas la app o cambias de teléfono, puedes " +
            "restaurar todo desde ese archivo.",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = {
                exportando = true
                vm.exportar(context) { nombre ->
                    exportando = false
                    val mensaje = if (nombre != null) "Copia guardada en Descargas: $nombre" else "No se pudo exportar"
                    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                }
            },
            enabled = !exportando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (exportando) "Exportando..." else "Exportar copia de seguridad")
        }

        Divider()

        Text("Restaurar desde una copia", style = MaterialTheme.typography.titleMedium)
        Text(
            "Selecciona el archivo .json que exportaste antes. Esto reemplaza todos los datos actuales de la app.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedButton(
            onClick = { selectorArchivo.launch(arrayOf("application/json", "*/*")) },
            enabled = !importando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (importando) "Restaurando..." else "Elegir archivo y restaurar")
        }
    }

    val uriAConfirmar = mostrarConfirmacionImportar
    if (uriAConfirmar != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionImportar = null },
            title = { Text("¿Restaurar copia de seguridad?") },
            text = {
                Text(
                    "Esto va a borrar todos los datos actuales de la app (categorías, productos, " +
                    "movimientos e IPV) y los va a reemplazar por los del archivo elegido. Esta acción no se puede deshacer.",
                    textAlign = TextAlign.Start
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacionImportar = null
                    importando = true
                    vm.importar(context, uriAConfirmar) { exito ->
                        importando = false
                        val mensaje = if (exito) "Datos restaurados correctamente" else "No se pudo restaurar el archivo"
                        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                    }
                }) { Text("Sí, restaurar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionImportar = null }) { Text("Cancelar") }
            }
        )
    }
}
