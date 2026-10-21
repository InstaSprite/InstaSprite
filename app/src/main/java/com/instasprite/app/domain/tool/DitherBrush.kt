package com.instasprite.app.domain.tool

import com.instasprite.app.R

enum class DitherPattern(val nameRes: Int, val threshold: Int) {
    Solid(R.string.dither_solid, 16),
    Shade87(R.string.dither_87, 14),
    Shade75(R.string.dither_75, 12),
    Shade50(R.string.dither_50, 8),
    Shade25(R.string.dither_25, 4),
    Shade12(R.string.dither_12, 2),
    Diagonal(R.string.dither_diagonal, 8);

    fun usesForeground(row: Int, col: Int): Boolean = when (this) {
        Solid -> true
        Diagonal -> ((row + col) and 3) < 2
        else -> BAYER[(row and 3) * 4 + (col and 3)] < threshold
    }

    companion object {
        private val BAYER = intArrayOf(0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5)
    }
}

data class DitherBrush(
    val pattern: DitherPattern = DitherPattern.Solid,
    val secondaryColor: Int = 0
) {
    fun colorAt(row: Int, col: Int, foreground: Int): Int =
        if (pattern.usesForeground(row, col)) foreground else secondaryColor
}
