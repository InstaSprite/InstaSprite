package com.instasprite.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.instasprite.app.data.model.ColorPaletteData
import kotlinx.coroutines.flow.Flow


@Dao
interface ColorPaletteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(palette: ColorPaletteData)

    @Query("SELECT * FROM palette_data")
    suspend fun getAllPalette(): List<ColorPaletteData>

    @Query("SELECT * FROM palette_data")
    fun getAllPaletteFlow(): Flow<List<ColorPaletteData>>

    @Query("SELECT * FROM palette_data WHERE name = :name")
    suspend fun getPaletteByName(name: String): ColorPaletteData?

    @Query("SELECT * FROM palette_data WHERE id = :id")
    suspend fun getPaletteById(id: Int): ColorPaletteData?

    @Query("DELETE FROM palette_data WHERE name = :name")
    suspend fun deletePaletteByName(name: String)

    @Query("DELETE FROM palette_data WHERE id = :id")
    suspend fun deletePaletteById(id: Int)

    @Query("UPDATE palette_data SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Int, isFavorite: Boolean)
}