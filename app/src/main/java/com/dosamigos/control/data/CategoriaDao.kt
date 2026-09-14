package com.dosamigos.control.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categorias ORDER BY nombre")
    fun getAll(): Flow<List<Categoria>>

    @Insert
    suspend fun insert(categoria: Categoria)

    @Update
    suspend fun update(categoria: Categoria)

    @Delete
    suspend fun delete(categoria: Categoria)

    @Query("DELETE FROM categorias")
    suspend fun deleteAll()
}
