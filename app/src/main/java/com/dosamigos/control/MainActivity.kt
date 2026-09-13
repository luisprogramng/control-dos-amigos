package com.dosamigos.control

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dosamigos.control.data.AppDatabase
import com.dosamigos.control.ui.*

sealed class Pantalla(val ruta: String, val titulo: String) {
    object Categorias : Pantalla("categorias", "Categorías")
    object Productos : Pantalla("productos", "Productos")
    object Movimientos : Pantalla("movimientos", "Movimientos")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getInstance(applicationContext)
        val factory = ViewModelFactory(db)

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val items = listOf(Pantalla.Categorias, Pantalla.Productos, Pantalla.Movimientos)

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val backStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = backStackEntry?.destination

                            items.forEach { pantalla ->
                                val icono = when (pantalla) {
                                    Pantalla.Categorias -> Icons.Default.Category
                                    Pantalla.Productos -> Icons.Default.Inventory
                                    Pantalla.Movimientos -> Icons.Default.SwapVert
                                }
                                NavigationBarItem(
                                    icon = { Icon(icono, contentDescription = pantalla.titulo) },
                                    label = { Text(pantalla.titulo) },
                                    selected = currentRoute?.hierarchy?.any { it.route == pantalla.ruta } == true,
                                    onClick = {
                                        navController.navigate(pantalla.ruta) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = Pantalla.Categorias.ruta,
                        modifier = androidx.compose.ui.Modifier.padding(padding)
                    ) {
                        composable(Pantalla.Categorias.ruta) {
                            CategoriaScreen(viewModel(factory = factory))
                        }
                        composable(Pantalla.Productos.ruta) {
                            ProductoScreen(viewModel(factory = factory))
                        }
                        composable(Pantalla.Movimientos.ruta) {
                            MovimientoScreen(viewModel(factory = factory))
                        }
                    }
                }
            }
        }
    }
}
