package com.olaz.instasprite.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.olaz.instasprite.SpritePixels
import com.olaz.instasprite.data.serializer.SpritePixelsSerializer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpritePixelDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStoreCache = mutableMapOf<String, DataStore<SpritePixels>>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getDataStore(spriteId: String): DataStore<SpritePixels> {
        return synchronized(dataStoreCache) {
            dataStoreCache.getOrPut(spriteId) {
                DataStoreFactory.create(
                    serializer = SpritePixelsSerializer,
                    scope = scope,
                    produceFile = { context.dataStoreFile("sprite_pixels_$spriteId.pb") }
                )
            }
        }
    }

    fun deleteDataStore(spriteId: String) {
        synchronized(dataStoreCache) {
            dataStoreCache.remove(spriteId)
        }
        val file = context.dataStoreFile("sprite_pixels_$spriteId.pb")
        if (file.exists()) {
            file.delete()
        }
    }
}
