package com.instasprite.app.domain.model

import android.graphics.Rect
import android.graphics.Region
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath

data class SelectionState(
    val mask: BooleanArray,
    val bounds: Rect,
    val canvasWidth: Int,
    val canvasHeight: Int
) {
    fun deepCopy(): SelectionState {
        return SelectionState(
            mask = mask.copyOf(),
            bounds = Rect(bounds),
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight
        )
    }

    companion object {
        fun computeBounds(mask: BooleanArray, w: Int, h: Int): Rect {
            var left = w
            var top = h
            var right = -1
            var bottom = -1
            for (r in 0 until h) {
                val offset = r * w
                for (c in 0 until w) {
                    if (mask[offset + c]) {
                        if (c < left) left = c
                        if (c > right) right = c
                        if (r < top) top = r
                        if (r > bottom) bottom = r
                    }
                }
            }
            if (right == -1) return Rect(0, 0, 0, 0)
            return Rect(left, top, right + 1, bottom + 1)
        }
    }

    val outlinePath: Path by lazy {
        val region = Region()
        val w = canvasWidth

        for (r in bounds.top until bounds.bottom) {
            val offset = r * w
            var startC = -1
            for (c in bounds.left..bounds.right) {
                val isSelected = c < bounds.right && mask[offset + c]
                if (isSelected && startC == -1) {
                    startC = c
                } else if (!isSelected && startC != -1) {
                    region.op(startC, r, c, r + 1, Region.Op.UNION)
                    startC = -1
                }
            }
        }

        val androidPath = android.graphics.Path()
        region.getBoundaryPath(androidPath)
        androidPath.asComposePath()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SelectionState

        if (!mask.contentEquals(other.mask)) return false
        if (bounds != other.bounds) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mask.contentHashCode()
        result = 31 * result + bounds.hashCode()
        return result
    }
}
