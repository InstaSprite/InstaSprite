package com.olaz.instasprite.data.repository

import android.util.Log
import com.olaz.instasprite.data.database.SpriteDataDao
import com.olaz.instasprite.data.database.SpriteMetaDataDao
import com.olaz.instasprite.data.mapper.toDomain
import com.olaz.instasprite.data.mapper.toEntity
import com.olaz.instasprite.data.model.SpriteMetaData
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.domain.model.SpriteMeta
import com.olaz.instasprite.domain.model.SpriteWithMeta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SpriteDatabaseRepository(
    private val dao: SpriteDataDao,
    private val metaDao: SpriteMetaDataDao
) {
    suspend fun saveSprite(sprite: Sprite) {
        Log.d("ISpriteDatabaseRepository", "Saving sprite: $sprite")
        val now = System.currentTimeMillis()
        val entity = sprite.toEntity()

        val existingMeta = metaDao.getMetaById(sprite.id)

        val meta = existingMeta?.copy(lastModifiedAt = now) ?:
            SpriteMetaData(
                spriteId = sprite.id,
                createdAt = now,
                lastModifiedAt = now
            )

        dao.insert(entity)
        metaDao.insert(meta)
    }

    suspend fun loadSprite(id: String): Sprite? {
        return dao.getById(id)?.toDomain()
    }

    suspend fun getSpriteList(): Pair<List<Sprite>, List<SpriteMeta>> {
        val sprites = dao.getAllSprites().map { it.toDomain() }
        val metas = metaDao.getAllMeta().map { it.toDomain() }
        return Pair(sprites, metas)
    }

    fun getAllSpritesWithMeta(): Flow<List<SpriteWithMeta>> {
        return dao.getAllSpritesWithMeta().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun deleteSpriteById(spriteId: String) {
        dao.delete(spriteId)
    }

    suspend fun changeName(spriteId: String, newName: String) {
        metaDao.changeSpriteName(spriteId, newName)
    }
}