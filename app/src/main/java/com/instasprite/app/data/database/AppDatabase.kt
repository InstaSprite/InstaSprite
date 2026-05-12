package com.instasprite.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.instasprite.app.data.model.ColorPaletteData
import com.instasprite.app.data.model.SpriteData
import com.instasprite.app.data.model.IntListConverter
import com.instasprite.app.data.model.SpriteMetaData

@Database(
    entities = [
        SpriteData::class,
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
