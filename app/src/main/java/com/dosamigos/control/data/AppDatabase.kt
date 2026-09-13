package com.dosamigos.control.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromTipo(tipo: TipoMovimiento): String = tipo.name

    @TypeConverter
    fun toTipo(value: String): TipoMovimiento = TipoMovimiento.valueOf(value)
}

@Database(
    entities = [Categoria::class, Producto::class, Movimiento::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoriaDao(): CategoriaDao
    abstract fun productoDao(): ProductoDao
    abstract fun movimientoDao(): MovimientoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "control_dos_amigos_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
