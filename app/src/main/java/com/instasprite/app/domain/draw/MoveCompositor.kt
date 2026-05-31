package com.instasprite.app.domain.draw

import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.SelectionState
import com.instasprite.app.utils.blendPixel
import com.instasprite.app.utils.tilesToPixels

class MoveCompositor(
    private val width: Int,
    private val height: Int,
    layers: List<Layer>,
    activeLayerId: String,
    selectionState: SelectionState?
) {
    private val transparentArgb = 0
    private val totalPixels = width * height

    val compositedBuffer = IntArray(totalPixels)
    private val staticBackground = IntArray(totalPixels)

    private val selectionMask = selectionState?.mask ?: BooleanArray(totalPixels) { true }
    private val selectionBounds = selectionState?.bounds ?: android.graphics.Rect(0, 0, width, height)
    private var originalSelectionPixels = IntArray(totalPixels)

    private class AboveLayerCache(
        val pixels: IntArray,
        val opacity: Float,
        val blendMode: com.instasprite.app.domain.model.BlendMode
    )
    private val aboveLayers = mutableListOf<AboveLayerCache>()

    private var activeOpacity = 1.0f
    private var activeBlendMode = com.instasprite.app.domain.model.BlendMode.NORMAL

    init {
        val activeIdx = layers.indexOfFirst { it.id == activeLayerId }
        val belowComposited = IntArray(totalPixels)
        for (i in 0 until activeIdx) {
            val layer = layers[i]
            if (!layer.isVisible) continue
            val layerPixels = tilesToPixels(layer.tiles, width, height)
            for (idx in 0 until totalPixels) {
                belowComposited[idx] = blendPixel(belowComposited[idx], layerPixels[idx], layer.opacity, layer.blendMode)
            }
        }

        if (activeIdx in layers.indices) {
            val activeLayer = layers[activeIdx]
            activeOpacity = activeLayer.opacity
            activeBlendMode = activeLayer.blendMode
            
            val activePixels = tilesToPixels(activeLayer.tiles, width, height)
            val clearedActive = activePixels.copyOf()
            
            for (i in 0 until totalPixels) {
                if (i < selectionMask.size && selectionMask[i]) {
                    originalSelectionPixels[i] = activePixels[i]
                    clearedActive[i] = 0
                }
            }

            System.arraycopy(belowComposited, 0, staticBackground, 0, totalPixels)
            for (i in 0 until totalPixels) {
                staticBackground[i] = blendPixel(staticBackground[i], clearedActive[i], activeOpacity, activeBlendMode)
            }
        } else {
            System.arraycopy(belowComposited, 0, staticBackground, 0, totalPixels)
        }

        for (i in (activeIdx + 1) until layers.size) {
            val layer = layers[i]
            if (!layer.isVisible) continue
            val layerPixels = tilesToPixels(layer.tiles, width, height)
            aboveLayers.add(AboveLayerCache(layerPixels, layer.opacity, layer.blendMode))
        }
    }

    fun composite(offsetX: Int, offsetY: Int): IntArray {
        System.arraycopy(staticBackground, 0, compositedBuffer, 0, totalPixels)

        val bounds = selectionBounds
        val rStart = bounds.top.coerceIn(0, height)
        val rEnd = bounds.bottom.coerceIn(0, height)
        val cStart = bounds.left.coerceIn(0, width)
        val cEnd = bounds.right.coerceIn(0, width)

        for (srcY in rStart until rEnd) {
            val srcOffset = srcY * width
            for (srcX in cStart until cEnd) {
                val srcIdx = srcOffset + srcX
                if (srcIdx >= selectionMask.size || !selectionMask[srcIdx]) continue
                val pixel = originalSelectionPixels[srcIdx]
                if (pixel == transparentArgb) continue

                val dstX = srcX + offsetX
                val dstY = srcY + offsetY
                if (dstX in 0 until width && dstY in 0 until height) {
                    val dstIdx = dstY * width + dstX
                    compositedBuffer[dstIdx] = blendPixel(compositedBuffer[dstIdx], pixel, activeOpacity, activeBlendMode)
                }
            }
        }

        for (above in aboveLayers) {
            val abovePixels = above.pixels
            val op = above.opacity
            val bm = above.blendMode
            for (i in 0 until totalPixels) {
                compositedBuffer[i] = blendPixel(compositedBuffer[i], abovePixels[i], op, bm)
            }
        }

        return compositedBuffer
    }
}
