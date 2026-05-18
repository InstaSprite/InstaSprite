package com.instasprite.app.domain.draw

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import com.instasprite.app.domain.model.SelectionState
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import kotlinx.coroutines.Dispatchers
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

    private fun clearSelectionBitmap() {
        _selectionBitmap?.eraseColor(Color.TRANSPARENT)
    }

    fun refreshSelectionBitmap(selectionState: SelectionState?) {
        ensureSelectionBitmap()
        if (selectionState != null) {
            val bmp = _selectionBitmap ?: return
            val mask = selectionState.mask
            val w = bmp.width
            val h = bmp.height
            val dimTint = 0x60000000 // dim background
            val pixels = IntArray(w * h)

            for (i in 0 until w * h) {
                if (!mask[i]) {
                    pixels[i] = dimTint
                }
            }

            bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        } else {
            clearSelectionBitmap()
        }
        selectionVersion++
    }

    suspend fun refreshBitmapState(): Long {
        val w = pixelCanvasUseCase.getCanvasWidth()
        val h = pixelCanvasUseCase.getCanvasHeight()

        val pixels = withContext(Dispatchers.Default) {
            pixelCanvasUseCase.getAllPixels()
        }

        ensureBitmap(w, h)
        if (pixels.size == w * h) {
            _bitmap?.setPixels(pixels, 0, w, 0, 0, w, h)
        }
        drawVersion++
        return drawVersion
    }

    fun refreshBitmapRegion(startRow: Int, startCol: Int, endRow: Int, endCol: Int): Long {
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
        drawVersion++
    }

    fun release() {
        _bitmap?.recycle()
        _bitmap = null
        _overlayBitmap?.recycle()
        _overlayBitmap = null
        _selectionBitmap?.recycle()
        _selectionBitmap = null
    }
}
