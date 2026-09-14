package com.dosamigos.control.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Converters {
    @TypeConverter
    fun fromTipo(tipo: TipoMovimiento): String = tipo.name

    @TypeConverter
    fun toTipo(value: String): TipoMovimiento = TipoMovimiento.valueOf(value)
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE productos ADD COLUMN activo INTEGER NOT NULL DEFAULT 1")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ipv (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                fecha INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ipv_detalle (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                ipvId INTEGER NOT NULL,
                productoId INTEGER,
                productoNombre TEXT NOT NULL,
                categoriaNombre TEXT,
                stockSistema INTEGER NOT NULL,
                stockContado INTEGER NOT NULL,
                diferencia INTEGER NOT NULL,
                FOREIGN KEY(ipvId) REFERENCES ipv(id) ON DELETE CASCADE,
                FOREIGN KEY(productoId) REFERENCES productos(id) ON DELETE SET NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE productos ADD COLUMN stockMinimo INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [Categoria::class, Producto::class, Movimiento::class, Ipv::class, IpvDetalle::class],
    version = 3
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoriaDao(): CategoriaDao
    abstract fun productoDao(): ProductoDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun ipvDao(): IpvDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "control_dos_amigos_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
