package com.dosamigos.control.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "productos",
    foreignKeys = [ForeignKey(
        entity = Categoria::class,
        parentColumns = ["id"],
        childColumns = ["categoriaId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val cantidadStock: Int,
    val precioCompra: Double,
    val precioVenta: Double,
    val categoriaId: Int?,
    val activo: Boolean = true,
    val stockMinimo: Int = 0
)
