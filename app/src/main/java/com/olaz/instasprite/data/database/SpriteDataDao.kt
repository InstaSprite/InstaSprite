package com.olaz.instasprite.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.olaz.instasprite.data.model.SpriteData
import com.olaz.instasprite.data.model.SpriteWithMetaData
import kotlinx.coroutines.flow.Flow

@Dao
interface SpriteDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sprite: SpriteData)

    @Query("SELECT * FROM sprite_data WHERE id = :id")
    suspend fun getById(id: String): SpriteData?

    @Query("SELECT * FROM sprite_data")
    suspend fun getAllSprites(): List<SpriteData>

    @Transaction
    @Query("SELECT * FROM sprite_data")
    fun getAllSpritesWithMeta(): Flow<List<SpriteWithMetaData>>

    @Query("DELETE FROM sprite_data WHERE id = :id")
    fun delete(id: String)

}