package com.dosamigos.control.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class ProductoConCategoria(
    val id: Int,
    val nombre: String,
    val cantidadStock: Int,
    val precioCompra: Double,
    val precioVenta: Double,
    val categoriaId: Int?,
    val categoriaNombre: String?
)

@Dao
interface ProductoDao {
    @Query("""
        SELECT p.*, c.nombre AS categoriaNombre FROM productos p
        LEFT JOIN categorias c ON p.categoriaId = c.id
        ORDER BY p.nombre
    """)
    fun getAllConCategoria(): Flow<List<ProductoConCategoria>>

    @Insert
    suspend fun insert(producto: Producto)

    @Update
    suspend fun update(producto: Producto)

    @Delete
    suspend fun delete(producto: Producto)

    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun getById(id: Int): Producto?

    @Query("UPDATE productos SET cantidadStock = cantidadStock + :delta WHERE id = :id")
    suspend fun ajustarStock(id: Int, delta: Int)
}
