package com.instasprite.app.domain.draw

import com.instasprite.app.domain.model.DrawingCheckpoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CanvasAutosave(
    private val persist: suspend (DrawingCheckpoint, Boolean) -> Unit,
    private val onFailure: (Boolean) -> Unit,
    private val onFailureCause: (Throwable) -> Unit = {}
) {
    private val mutex = Mutex()
    private var latest: DrawingCheckpoint? = null
    private var revision = 0L
    private var savedRevision = -1L
    private var failureReported = false

    fun update(checkpoint: DrawingCheckpoint) {
        latest = checkpoint
        revision++
    }

    fun updatePalette(colors: List<Int>) {
        val checkpoint = latest ?: return
        if (checkpoint.sprite.colorPalette != colors) {
            update(checkpoint.copy(sprite = checkpoint.sprite.copy(colorPalette = colors)))
        }
    }

    suspend fun flush(finished: Boolean = false): Boolean = mutex.withLock {
        val checkpoint = latest ?: return@withLock false
        if (!finished && savedRevision == revision) return@withLock true
        val savingRevision = revision
        try {
            persist(checkpoint.copy(unfinishedSession = !finished), finished)
            savedRevision = savingRevision
            failureReported = false
            onFailure(false)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: OutOfMemoryError) {
            onFailure(true)
            false
        } catch (e: Exception) {
            if (!failureReported) {
                failureReported = true
                onFailureCause(e)
            }
            onFailure(true)
            false
        }
    }
}
