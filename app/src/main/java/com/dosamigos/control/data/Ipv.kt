package com.dosamigos.control.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ipv")
data class Ipv(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: Long = System.currentTimeMillis()
)
