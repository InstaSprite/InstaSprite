package com.instasprite.app.data.repository

import android.util.Log
import com.instasprite.app.data.database.SpriteDataDao
import com.instasprite.app.data.database.SpriteMetaDataDao
import com.instasprite.app.data.mapper.toDomain
import com.instasprite.app.data.mapper.toEntity
import com.instasprite.app.data.mapper.toISprite
import com.instasprite.app.data.mapper.toSprite
import com.instasprite.app.data.model.SpriteMetaData
import com.instasprite.app.data.source.ISpriteDatSource
import com.instasprite.app.domain.export.ImageExporter
import com.instasprite.app.domain.model.DrawingCheckpoint
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.model.SpriteMeta
import com.instasprite.app.domain.model.SpriteWithMeta
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.IOException
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

class SpriteDatabaseRepository @Inject constructor(
    private val dao: SpriteDataDao,
    private val metaDao: SpriteMetaDataDao,
    private val pixelDataSource: ISpriteDatSource,
    @ApplicationContext private val context: android.content.Context
) {
    private val fileMutex = Mutex()

    suspend fun saveSprite(sprite: Sprite) = saveDrawing(DrawingCheckpoint(sprite), true)

    suspend fun saveDrawing(checkpoint: DrawingCheckpoint, updateThumbnail: Boolean) = fileMutex.withLock {
        recoverPendingSaves()
        val sprite = checkpoint.sprite
        Log.d("ISpriteDatabaseRepository", "Saving sprite: ${sprite.id}")
        val now = System.currentTimeMillis()
        val entity = sprite.toEntity()

        val existingMeta = metaDao.getMetaById(sprite.id)
        val meta = existingMeta?.copy(lastModifiedAt = now) ?: SpriteMetaData(
            spriteId = sprite.id,
            createdAt = now,
            lastModifiedAt = now
        )

        val token = UUID.randomUUID().toString()
        val proto = sprite.toISprite().toBuilder()
            .setUnfinishedSession(checkpoint.unfinishedSession)
            .setActiveFrameId(checkpoint.activeFrameId)
            .setActiveLayerId(checkpoint.activeLayerId)
            .setSaveToken(token)
            .setSavedName(meta.spriteName)
            .setSavedCreatedAt(meta.createdAt)
            .setSavedModifiedAt(meta.lastModifiedAt)
            .build()
        val pending = File(context.filesDir, "pending_saves/${sprite.id}")
        if (!pending.parentFile!!.isDirectory && !pending.parentFile!!.mkdirs()) throw IOException("Could not create save journal")
        FileOutputStream(pending).use { output ->
            output.write(token.toByteArray(Charsets.UTF_8))
            output.fd.sync()
        }
        pixelDataSource.writeData(sprite.id, proto)
        dao.save(entity, meta)
        if (!pending.delete()) throw IOException("Could not finish save journal")
        if (updateThumbnail) ImageExporter.saveThumbnail(sprite, context)
    }

    private suspend fun recoverPendingSaves() {
        File(context.filesDir, "pending_saves").listFiles().orEmpty().forEach { pending ->
            if (!pending.isFile) return@forEach
            val proto = pixelDataSource.readData(pending.name)
            if (proto.saveToken.isNotEmpty() && proto.saveToken == pending.readText()) {
                require(proto.width > 0 && proto.height > 0)
                val existing = metaDao.getMetaById(pending.name)
                dao.save(
                    com.instasprite.app.data.model.SpriteData(pending.name, proto.width, proto.height, proto.colorPaletteList, proto.framesCount.coerceAtLeast(1)),
                    SpriteMetaData(
                        pending.name, existing?.spriteName ?: proto.savedName,
                        existing?.createdAt ?: proto.savedCreatedAt,
                        maxOf(existing?.lastModifiedAt ?: 0, proto.savedModifiedAt)
                    )
                )
            }
            if (!pending.delete()) throw IOException("Could not clear pending save")
        }
    }

    suspend fun loadSprite(id: String): Sprite? = loadDrawing(id)?.sprite

    suspend fun loadDrawing(id: String): DrawingCheckpoint? {
        val entity = dao.getById(id) ?: return null
        val proto = pixelDataSource.readData(id)

        if (proto != null) {
            val sprite = proto.toSprite()

            return DrawingCheckpoint(
                sprite.copy(id = entity.id), proto.activeFrameId, proto.activeLayerId,
                proto.unfinishedSession
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
                item.toDomain()
            }
        }
    }

    suspend fun deleteSpriteById(spriteId: String) = fileMutex.withLock {
        val pending = File(context.filesDir, "pending_saves/$spriteId")
        if (pending.exists() && !pending.delete()) throw IOException("Could not clear pending save")
        dao.delete(spriteId)
        deleteSpriteFiles(spriteId)
    }

    suspend fun cleanupOrphanedSpriteFiles() = fileMutex.withLock {
        recoverPendingSaves()
        val sprites = dao.getAllSprites()
        for (sprite in sprites.filter { it.frameCount == 0 }) {
            try {
                val proto = pixelDataSource.readData(sprite.id)
                dao.insert(sprite.copy(colorPalette = proto.colorPaletteList, frameCount = proto.framesCount.coerceAtLeast(1)))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("SpriteDatabaseRepository", "Could not backfill sprite summary", e)
            }
        }
        val activeIds = sprites.mapTo(mutableSetOf()) { it.id }
        val thumbnailIds = context.filesDir.listFiles().orEmpty()
            .mapNotNull { Regex("thumbnail_(.+)\\.png").matchEntire(it.name)?.groupValues?.get(1) }
        val pixelIds = File(context.filesDir, "datastore").listFiles().orEmpty()
            .mapNotNull { Regex("sprite_pixels_(.+)\\.pb(?:\\.tmp)?").matchEntire(it.name)?.groupValues?.get(1) }
        for (id in (thumbnailIds + pixelIds).toSet() - activeIds) {
            try {
                deleteSpriteFiles(id)
            } catch (e: IOException) {
                Log.w("SpriteDatabaseRepository", "Could not clean orphaned sprite files", e)
            }
        }
    }

    private suspend fun deleteSpriteFiles(spriteId: String) {
        pixelDataSource.deleteDataStore(spriteId)
        val history = File(context.cacheDir, "history/$spriteId")
        if (history.exists() && !history.deleteRecursively()) throw IOException("Could not delete sprite undo history")
        val thumbnail = File(context.filesDir, "thumbnail_${spriteId}.png")
        if (thumbnail.exists() && !thumbnail.delete()) throw IOException("Could not delete sprite thumbnail")
    }

    suspend fun changeName(spriteId: String, newName: String) {
        metaDao.changeSpriteName(spriteId, newName)
    }
}
