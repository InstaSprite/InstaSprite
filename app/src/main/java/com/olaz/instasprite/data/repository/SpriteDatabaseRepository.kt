package com.olaz.instasprite.data.repository

import android.util.Log
import com.olaz.instasprite.data.database.SpriteDataDao
import com.olaz.instasprite.data.database.SpriteMetaDataDao
import com.olaz.instasprite.data.mapper.toDomain
import com.olaz.instasprite.data.mapper.toEntity
import com.olaz.instasprite.data.mapper.toSpritePixels
import com.olaz.instasprite.data.model.SpriteMetaData
import com.olaz.instasprite.data.source.SpritePixelDataSource
import com.olaz.instasprite.domain.export.ImageExporter
import com.olaz.instasprite.domain.model.Layer
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.domain.model.SpriteMeta
import com.olaz.instasprite.domain.model.SpriteWithMeta
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

class SpriteDatabaseRepository @Inject constructor(
    private val dao: SpriteDataDao,
    private val metaDao: SpriteMetaDataDao,
    private val pixelDataSource: SpritePixelDataSource,
    @ApplicationContext private val context: android.content.Context
) {
    suspend fun saveSprite(sprite: Sprite) {
        Log.d("ISpriteDatabaseRepository", "Saving sprite: $sprite")
        val now = System.currentTimeMillis()
        val entity = sprite.toEntity()

        val existingMeta = metaDao.getMetaById(sprite.id)
        val meta = existingMeta?.copy(lastModifiedAt = now) ?: SpriteMetaData(
            spriteId = sprite.id,
            createdAt = now,
            lastModifiedAt = now
        )

        val proto = sprite.toSpritePixels()
        pixelDataSource.getDataStore(sprite.id).updateData { proto }

        ImageExporter.saveThumbnail(sprite, context)

        dao.insert(entity)
        metaDao.insert(meta)
    }

    suspend fun loadSprite(id: String): Sprite? {
        val entity = dao.getById(id) ?: return null
        val proto = pixelDataSource.getDataStore(id).data.firstOrNull()

        if (proto != null) {
            val layers = proto.layersList.map { layerData ->
                Layer(
                    id = layerData.id,
                    name = layerData.name,
                    isVisible = layerData.isVisible,
                    isLocked = layerData.isLocked,
                    pixels = layerData.pixelsList.toIntArray()
                )
            }

            return entity.toDomain(
                layers = layers,
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
            list.map { it.toDomain() }
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