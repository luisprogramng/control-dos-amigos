package com.dosamigos.control.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dosamigos.control.data.AppDatabase

class ViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            CategoriaViewModel::class.java -> CategoriaViewModel(db) as T
            ProductoViewModel::class.java -> ProductoViewModel(db) as T
            MovimientoViewModel::class.java -> MovimientoViewModel(db) as T
            IpvViewModel::class.java -> IpvViewModel(db) as T
            else -> throw IllegalArgumentException("ViewModel desconocido")
        }
    }
}
