package com.instasprite.app.data.repository

import android.util.Log
import com.instasprite.app.data.database.SpriteDataDao
import com.instasprite.app.data.database.SpriteMetaDataDao
import com.instasprite.app.data.mapper.toDomain
import com.instasprite.app.data.mapper.toEntity
import com.instasprite.app.data.mapper.toSprite
import com.instasprite.app.data.mapper.toISprite
import com.instasprite.app.data.model.SpriteMetaData
import com.instasprite.app.data.source.ISpriteDatSource
import com.instasprite.app.domain.export.ImageExporter
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.model.SpriteMeta
import com.instasprite.app.domain.model.SpriteWithMeta
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

class SpriteDatabaseRepository @Inject constructor(
    private val dao: SpriteDataDao,
    private val metaDao: SpriteMetaDataDao,
    private val pixelDataSource: ISpriteDatSource,
    @ApplicationContext private val context: android.content.Context
) {
    suspend fun saveSprite(sprite: Sprite) {
        Log.d("ISpriteDatabaseRepository", "Saving sprite: ${sprite.id}")
        val now = System.currentTimeMillis()
        val entity = sprite.toEntity()

        val existingMeta = metaDao.getMetaById(sprite.id)
        val meta = existingMeta?.copy(lastModifiedAt = now) ?: SpriteMetaData(
            spriteId = sprite.id,
            createdAt = now,
            lastModifiedAt = now
        )

        val proto = sprite.toISprite()
        pixelDataSource.getDataStore(sprite.id).updateData { proto }

        ImageExporter.saveThumbnail(sprite, context)

        dao.insert(entity)
        metaDao.insert(meta)
    }

    suspend fun loadSprite(id: String): Sprite? {
        val entity = dao.getById(id) ?: return null
        val proto = pixelDataSource.getDataStore(id).data.firstOrNull()

        if (proto != null) {
            val sprite = proto.toSprite()

            return entity.toDomain(
                layers = sprite.layers,
                colorPalette = proto.colorPaletteList,
            )
        }

        return null
    }

    suspend fun getSpriteList(): Pair<List<Sprite>, List<SpriteMeta>> {
        val sprites = dao.getAllSprites().map { it.toDomain() }
        val metas = metaDao.getAllMeta().map { it.toDomain() }
        return Pair(sprites, metas)
    }

    fun getAllSpritesWithMeta(): Flow<List<SpriteWithMeta>> {
        return dao.getAllSpritesWithMeta().map { list ->
            list.map { item ->
                val proto = pixelDataSource.getDataStore(item.sprite.id).data.firstOrNull()
                item.toDomain(
                    sprite = item.sprite.toDomain(
                        layers = emptyList(),
                        colorPalette = proto?.colorPaletteList
                    )
                )
            }
        }
    }

    fun deleteSpriteById(spriteId: String) {
        dao.delete(spriteId)
        pixelDataSource.deleteDataStore(spriteId)

        val thumbnailFile = File(context.filesDir, "thumbnail_${spriteId}.png")
        if (thumbnailFile.exists()) {
            thumbnailFile.delete()
        }
    }

    suspend fun changeName(spriteId: String, newName: String) {
        metaDao.changeSpriteName(spriteId, newName)
    }
}