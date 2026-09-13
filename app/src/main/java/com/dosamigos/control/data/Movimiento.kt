package com.dosamigos.control.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

enum class TipoMovimiento { ENTRADA, SALIDA }

@Entity(
    tableName = "movimientos",
    foreignKeys = [ForeignKey(
        entity = Producto::class,
        parentColumns = ["id"],
        childColumns = ["productoId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productoId: Int,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val fecha: Long = System.currentTimeMillis(),
    val nota: String = ""
)
