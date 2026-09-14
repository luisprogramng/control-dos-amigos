package com.dosamigos.control.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class IpvConDetalles(
    @Embedded val ipv: Ipv,
    @Relation(parentColumn = "id", entityColumn = "ipvId")
    val detalles: List<IpvDetalle>
)

@Dao
interface IpvDao {
    @Insert
    suspend fun insertIpv(ipv: Ipv): Long

    @Insert
    suspend fun insertDetalles(detalles: List<IpvDetalle>)

    @Transaction
    @Query("SELECT * FROM ipv ORDER BY fecha DESC")
    fun getHistorial(): Flow<List<IpvConDetalles>>

    @Transaction
    @Query("SELECT * FROM ipv WHERE id = :id")
    suspend fun getPorId(id: Int): IpvConDetalles?

    @Query("DELETE FROM ipv")
    suspend fun deleteAllIpv()

    @Query("DELETE FROM ipv_detalle")
    suspend fun deleteAllDetalles()
}
