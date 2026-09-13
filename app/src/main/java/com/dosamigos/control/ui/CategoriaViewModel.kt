package com.dosamigos.control.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dosamigos.control.data.AppDatabase
import com.dosamigos.control.data.Categoria
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriaViewModel(private val db: AppDatabase) : ViewModel() {
    val categorias = db.categoriaDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregar(nombre: String) = viewModelScope.launch {
        db.categoriaDao().insert(Categoria(nombre = nombre))
    }

    fun actualizar(categoria: Categoria) = viewModelScope.launch {
        db.categoriaDao().update(categoria)
    }

    fun eliminar(categoria: Categoria) = viewModelScope.launch {
        db.categoriaDao().delete(categoria)
    }
}
