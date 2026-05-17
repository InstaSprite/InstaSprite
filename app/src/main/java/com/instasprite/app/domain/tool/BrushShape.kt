package com.instasprite.app.domain.tool

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import com.instasprite.app.R

enum class BrushShape(val icon: Int) {
    Circle(R.drawable.ic_circle_tool),
    Square(R.drawable.ic_rect_tool)
}

class BrushStamp private constructor(
    val offsets: IntArray,
    val count: Int
) {
    companion object {
        private val cache = mutableMapOf<Pair<BrushShape, Int>, BrushStamp>()

        fun create(shape: BrushShape, size: Int): BrushStamp {
            val key = Pair(shape, size)
            var stamp = cache[key]
            if (stamp == null) {
                stamp = when (shape) {
                    BrushShape.Square -> createSquare(size)
                    BrushShape.Circle -> createCircle(size)
                }
                cache[key] = stamp
            }
            return stamp
        }

        private fun createSquare(size: Int): BrushStamp {
            val offsets = IntArray(size * size * 2)
            var i = 0
            val half = (size - 1) / 2

            var rStart = -half
            var rEnd = -half + size - 1
            var cStart = -half
            var cEnd = -half + size - 1

            if (size % 2 == 0) {
                rEnd = half
                cEnd = half
                rStart = rEnd - size + 1
                cStart = cEnd - size + 1
            }

            for (dr in rStart..rEnd) {
                for (dc in cStart..cEnd) {
                    offsets[i++] = dr
                    offsets[i++] = dc
                }
            }
            return BrushStamp(offsets, size * size)
        }

        private fun createCircle(size: Int): BrushStamp {
            if (size <= 2) return createSquare(size)

            val maxOffsets = size * size * 2
            val offsets = IntArray(maxOffsets)
            var i = 0

            val half = (size - 1) / 2
            val rStart: Int
            val rEnd: Int
            val cStart: Int
            val cEnd: Int

            if (size % 2 == 0) {
                rEnd = half
                cEnd = half
                rStart = rEnd - size + 1
                cStart = cEnd - size + 1
            } else {
                rStart = -half
                rEnd = half
                cStart = -half
                cEnd = half
            }

            val r = size / 2f - 0.1f
            val radiusSq = r * r

            val centerR = (rStart + rEnd) / 2f
            val centerC = (cStart + cEnd) / 2f

            for (dr in rStart..rEnd) {
                for (dc in cStart..cEnd) {
                    val distR = dr - centerR
                    val distC = dc - centerC
                    if (distR * distR + distC * distC <= radiusSq) {
                        offsets[i++] = dr
                        offsets[i++] = dc
                    }
                }
            }
            return BrushStamp(offsets, i / 2)
        }
    }

    inline fun forEach(
        centerRow: Int,
        centerCol: Int,
        canvasWidth: Int,
        canvasHeight: Int,
        action: (row: Int, col: Int) -> Unit
    ) {
        for (i in 0 until count) {
            val r = centerRow + offsets[i * 2]
            val c = centerCol + offsets[i * 2 + 1]
            if (r in 0 until canvasHeight && c in 0 until canvasWidth) {
                action(r, c)
            }
        }
    }

    private var cachedPath: Path? = null
    private var cachedPathW: Float = 0f
    private var cachedPathH: Float = 0f

    fun createPath(cellWidth: Float, cellHeight: Float): Path {
        if (cachedPath != null && cachedPathW == cellWidth && cachedPathH == cellHeight) {
            return cachedPath!!
        }

        val androidPath = android.graphics.Path()
        val tempPath = android.graphics.Path()
        for (i in 0 until count) {
            val r = offsets[i * 2]
            val c = offsets[i * 2 + 1]
            tempPath.reset()
            tempPath.addRect(
                c * cellWidth,
                r * cellHeight,
                (c + 1) * cellWidth,
                (r + 1) * cellHeight,
                android.graphics.Path.Direction.CW
            )
            androidPath.op(tempPath, android.graphics.Path.Op.UNION)
        }
        val composePath = androidPath.asComposePath()
        cachedPath = composePath
        cachedPathW = cellWidth
        cachedPathH = cellHeight
        return composePath
    }
}
