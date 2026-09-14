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
    val activo: Boolean,
    val stockMinimo: Int,
    val categoriaNombre: String?
)

@Dao
interface ProductoDao {
    @Query("""
        SELECT p.*, c.nombre AS categoriaNombre FROM productos p
        LEFT JOIN categorias c ON p.categoriaId = c.id
        ORDER BY p.activo DESC, p.nombre
    """)
    fun getAllConCategoria(): Flow<List<ProductoConCategoria>>

    @Query("""
        SELECT p.*, c.nombre AS categoriaNombre FROM productos p
        LEFT JOIN categorias c ON p.categoriaId = c.id
        WHERE p.activo = 1
        ORDER BY p.nombre
    """)
    fun getActivosConCategoria(): Flow<List<ProductoConCategoria>>

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

    @Query("UPDATE productos SET cantidadStock = :nuevoStock WHERE id = :id")
    suspend fun setStock(id: Int, nuevoStock: Int)

    @Query("UPDATE productos SET activo = :activo WHERE id = :id")
    suspend fun setActivo(id: Int, activo: Boolean)

    @Query("UPDATE productos SET stockMinimo = :stockMinimo WHERE id = :id")
    suspend fun setStockMinimo(id: Int, stockMinimo: Int)

    @Query("DELETE FROM productos")
    suspend fun deleteAll()
}
