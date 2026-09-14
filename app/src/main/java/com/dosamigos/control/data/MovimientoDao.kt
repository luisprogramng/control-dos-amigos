package com.dosamigos.control.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class MovimientoConProducto(
    val id: Int,
    val productoId: Int,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val fecha: Long,
    val nota: String,
    val productoNombre: String
)

@Dao
interface MovimientoDao {
    @Query("""
        SELECT m.*, p.nombre AS productoNombre FROM movimientos m
        JOIN productos p ON m.productoId = p.id
        ORDER BY m.fecha DESC
    """)
    fun getAllConProducto(): Flow<List<MovimientoConProducto>>

    @Query("""
        SELECT m.*, p.nombre AS productoNombre FROM movimientos m
        JOIN productos p ON m.productoId = p.id
        WHERE m.productoId = :productoId
        ORDER BY m.fecha DESC
    """)
    fun getPorProducto(productoId: Int): Flow<List<MovimientoConProducto>>

    @Insert
    suspend fun insert(movimiento: Movimiento)

    @Delete
    suspend fun delete(movimiento: Movimiento)

    @Query("DELETE FROM movimientos")
    suspend fun deleteAll()
}
