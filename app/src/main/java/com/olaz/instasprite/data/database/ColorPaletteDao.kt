package com.olaz.instasprite.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olaz.instasprite.data.model.ColorPaletteData


@Dao
interface ColorPaletteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(palette: ColorPaletteData)

    @Query("SELECT * FROM palette_data")
    suspend fun getAllPalette(): List<ColorPaletteData>

    @Query("SELECT * FROM palette_data WHERE name = :name")
    suspend fun getPaletteByName(name: String): ColorPaletteData?

    @Query("DELETE FROM palette_data WHERE name = :name")
    suspend fun deletePaletteByName(name: String)


}