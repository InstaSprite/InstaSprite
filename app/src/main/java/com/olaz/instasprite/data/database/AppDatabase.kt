package com.olaz.instasprite.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.olaz.instasprite.data.model.ColorPaletteData
import com.olaz.instasprite.data.model.ISpriteData
import com.olaz.instasprite.data.model.IntListConverter
import com.olaz.instasprite.data.model.SpriteMetaData

@Database(
    entities = [
        ISpriteData::class,
        SpriteMetaData::class,
        ColorPaletteData::class
    ], version = 1, exportSchema = false
)
@TypeConverters(
    value = [
        IntListConverter::class,
        ColorPaletteConverters::class
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun spriteDataDao(): SpriteDataDao

    abstract fun spriteMetaDataDao(): SpriteMetaDataDao
    abstract fun colorPaletteDao(): ColorPaletteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "instasprite_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
