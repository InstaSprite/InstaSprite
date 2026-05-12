package com.instasprite.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.instasprite.app.data.model.SpriteMetaData

@Dao
interface SpriteMetaDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meta: SpriteMetaData)

    @Query("SELECT * FROM sprite_metadata WHERE spriteId = :id")
    suspend fun getMetaById(id: String): SpriteMetaData?

    @Update
    suspend fun update(meta: SpriteMetaData)

    @Query("SELECT * FROM sprite_metadata")
    suspend fun getAllMeta(): List<SpriteMetaData>

    @Query("UPDATE sprite_metadata SET spriteName = :newName WHERE spriteId = :id")
    suspend fun changeSpriteName(id: String, newName: String)

}