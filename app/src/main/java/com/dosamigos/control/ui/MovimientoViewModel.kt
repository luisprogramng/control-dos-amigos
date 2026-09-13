package com.dosamigos.control.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dosamigos.control.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MovimientoViewModel(private val db: AppDatabase) : ViewModel() {
    val movimientos = db.movimientoDao().getAllConProducto()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productos = db.productoDao().getAllConCategoria()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun registrar(productoId: Int, tipo: TipoMovimiento, cantidad: Int, nota: String) =
        viewModelScope.launch {
            db.movimientoDao().insert(Movimiento(productoId = productoId, tipo = tipo, cantidad = cantidad, nota = nota))
            val delta = if (tipo == TipoMovimiento.ENTRADA) cantidad else -cantidad
            db.productoDao().ajustarStock(productoId, delta)
        }

    fun eliminar(movimiento: Movimiento) = viewModelScope.launch {
        db.movimientoDao().delete(movimiento)
    }
}
