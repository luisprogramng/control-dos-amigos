package com.dosamigos.control.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dosamigos.control.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductoViewModel(private val db: AppDatabase) : ViewModel() {
    val productos = db.productoDao().getAllConCategoria()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categorias = db.categoriaDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregar(nombre: String, stock: Int, pCompra: Double, pVenta: Double, categoriaId: Int?) =
        viewModelScope.launch {
            db.productoDao().insert(
                Producto(nombre = nombre, cantidadStock = stock, precioCompra = pCompra, precioVenta = pVenta, categoriaId = categoriaId)
            )
        }

    fun actualizar(producto: Producto) = viewModelScope.launch {
        db.productoDao().update(producto)
    }

    fun eliminar(producto: Producto) = viewModelScope.launch {
        db.productoDao().delete(producto)
    }
}
