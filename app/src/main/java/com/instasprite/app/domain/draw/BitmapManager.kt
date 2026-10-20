package com.instasprite.app.domain.draw

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import com.instasprite.app.domain.model.SelectionState
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import com.instasprite.app.utils.TILE_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class BitmapManager(
    private val pixelCanvasUseCase: PixelCanvasUseCase
) {
    private var _bitmap: Bitmap? = null
    val bitmap: Bitmap? get() = _bitmap
    var drawVersion: Long = 0
        private set

    private var _overlayBitmap: Bitmap? = null
    val overlayBitmap: Bitmap? get() = _overlayBitmap
    var overlayVersion: Long = 0
        private set

    private var _selectionBitmap: Bitmap? = null
    val selectionBitmap: Bitmap? get() = _selectionBitmap
    var selectionVersion: Long = 0
        private set

    private fun ensureBitmap(width: Int, height: Int) {
        if (_bitmap == null || _bitmap!!.width != width || _bitmap!!.height != height) {
            _bitmap?.recycle()
            _bitmap = if (width > 0 && height > 0) createBitmap(width, height) else null
        }
    }

    fun ensureOverlayBitmap() {
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()
        if (_overlayBitmap == null || _overlayBitmap!!.width != w || _overlayBitmap!!.height != h) {
            _overlayBitmap?.recycle()
            _overlayBitmap = if (w > 0 && h > 0) createBitmap(w, h) else null
        }
    }

    fun clearOverlayBitmap() {
        _overlayBitmap?.eraseColor(Color.TRANSPARENT)
    }

    fun releaseOverlayBitmap() {
        _overlayBitmap = null
        overlayVersion++
    }

    fun incrementOverlayVersion() {
        overlayVersion++
    }

    private fun ensureSelectionBitmap() {
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()
        if (_selectionBitmap == null || _selectionBitmap!!.width != w || _selectionBitmap!!.height != h) {
            _selectionBitmap?.recycle()
            _selectionBitmap = if (w > 0 && h > 0) createBitmap(w, h) else null
        }
    }

    fun refreshSelectionBitmap(selectionState: SelectionState?) {
        if (selectionState != null) {
            ensureSelectionBitmap()
            val bmp = _selectionBitmap ?: return
            val mask = selectionState.mask
            val w = bmp.width
            val h = bmp.height
            val blockHeight = minOf(TILE_SIZE, h)
            val pixels = IntArray(w * blockHeight)
            for (row in 0 until h step blockHeight) {
                val rows = minOf(blockHeight, h - row)
                for (i in 0 until w * rows) {
                    pixels[i] = if (mask[row * w + i]) 0 else 0x60000000
                }
                bmp.setPixels(pixels, 0, w, 0, row, w, rows)
            }
        } else {
            _selectionBitmap = null
        }
        selectionVersion++
    }

    private var renderRequest = 0L
    private val renderMutex = Mutex()
    private var renderPixels = IntArray(0)

    suspend fun refreshBitmapState(): Long {
        val request = ++renderRequest
        val snapshot = pixelCanvasUseCase.getSprite()
        val frameId = pixelCanvasUseCase.activeFrameId
        val w = snapshot.width
        val h = snapshot.height

        return renderMutex.withLock {
            if (request != renderRequest || frameId != pixelCanvasUseCase.activeFrameId) return@withLock drawVersion
            val pixelCount = Math.multiplyExact(w, h)
            if (renderPixels.size != pixelCount) renderPixels = IntArray(pixelCount)
            val pixels = renderPixels
            withContext(Dispatchers.Default) {
                snapshot.renderFrame(frameId, pixels)
            }
            if (request != renderRequest || frameId != pixelCanvasUseCase.activeFrameId) return@withLock drawVersion
            ensureBitmap(w, h)
            _bitmap?.setPixels(pixels, 0, w, 0, 0, w, h)
            drawVersion++
            drawVersion
        }
    }

    fun refreshBitmapRegion(startRow: Int, startCol: Int, endRow: Int, endCol: Int): Long {
        renderRequest++
        val bmp = _bitmap ?: return drawVersion
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()

        val r0 = startRow.coerceAtLeast(0)
        val c0 = startCol.coerceAtLeast(0)
        val r1 = endRow.coerceAtMost(h - 1)
        val c1 = endCol.coerceAtMost(w - 1)

        val regionW = c1 - c0 + 1
        val regionH = r1 - r0 + 1
        if (regionW <= 0 || regionH <= 0) return drawVersion

        val regionPixels = pixelCanvasUseCase.getAllPixelsInRegion(r0, c0, regionH, regionW)
        bmp.setPixels(regionPixels, 0, regionW, c0, r0, regionW, regionH)
        drawVersion++
        return drawVersion
    }

    fun incrementDrawVersion() {
        renderRequest++
        drawVersion++
    }

    fun release() {
        renderRequest++
        renderPixels = IntArray(0)
        _bitmap?.recycle()
        _bitmap = null
        _overlayBitmap?.recycle()
        _overlayBitmap = null
        _selectionBitmap?.recycle()
        _selectionBitmap = null
    }
}
