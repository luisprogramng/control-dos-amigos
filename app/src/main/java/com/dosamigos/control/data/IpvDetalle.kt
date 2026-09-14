package com.dosamigos.control.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "ipv_detalle",
    foreignKeys = [
        ForeignKey(
            entity = Ipv::class,
            parentColumns = ["id"],
            childColumns = ["ipvId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class IpvDetalle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ipvId: Int,
    val productoId: Int?,
    val productoNombre: String,
    val categoriaNombre: String?,
    val stockSistema: Int,
    val stockContado: Int,
    val diferencia: Int
)
