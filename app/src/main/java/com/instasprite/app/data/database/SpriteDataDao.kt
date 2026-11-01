package com.instasprite.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.instasprite.app.data.model.SpriteMetaData
import com.instasprite.app.data.model.SpriteData
import com.instasprite.app.data.model.SpriteWithMetaData
import kotlinx.coroutines.flow.Flow

@Dao
interface SpriteDataDao {
    @Upsert
    suspend fun insert(sprite: SpriteData)

    @Upsert
    suspend fun insertMetadata(meta: SpriteMetaData)

    @Transaction
    suspend fun save(sprite: SpriteData, meta: SpriteMetaData) {
        insert(sprite)
        insertMetadata(meta)
    }

    @Query("SELECT * FROM sprite_data WHERE id = :id")
    suspend fun getById(id: String): SpriteData?

    @Query("SELECT * FROM sprite_data")
    suspend fun getAllSprites(): List<SpriteData>

    @Transaction
    @Query("SELECT sprite_data.* FROM sprite_data LEFT JOIN sprite_metadata ON sprite_data.id = sprite_metadata.spriteId")
    fun getAllSpritesWithMeta(): Flow<List<SpriteWithMetaData>>

    @Query("DELETE FROM sprite_data WHERE id = :id")
    fun delete(id: String)

}