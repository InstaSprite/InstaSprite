package com.instasprite.app.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.instasprite.app.ISprite
import com.instasprite.app.data.serializer.ISpriteSerializer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ISpriteDatSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mutex = Mutex()

    suspend fun readData(spriteId: String): ISprite = withDataStore(spriteId) { it.data.first() }

    suspend fun writeData(spriteId: String, sprite: ISprite) {
        withDataStore(spriteId) { it.updateData { sprite } }
    }

    private suspend fun <T> withDataStore(spriteId: String, block: suspend (DataStore<ISprite>) -> T): T =
        mutex.withLock {
            val job = SupervisorJob()
            try {
                val store = DataStoreFactory.create(
                    serializer = ISpriteSerializer,
                    scope = CoroutineScope(Dispatchers.IO + job),
                    produceFile = { context.dataStoreFile("sprite_pixels_$spriteId.pb") }
                )
                block(store)
            } finally {
                withContext(NonCancellable) { job.cancelAndJoin() }
            }
        }

    suspend fun deleteDataStore(spriteId: String) = mutex.withLock {
        val file = context.dataStoreFile("sprite_pixels_$spriteId.pb")
        if (file.exists()) file.delete()
        Unit
    }
}
