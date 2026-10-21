package com.instasprite.app.domain.tool

import com.instasprite.app.R

enum class DitherPattern(val nameRes: Int, val threshold: Int) {
    Solid(R.string.dither_solid, 16),
    Shade87(R.string.dither_87, 14),
    Shade75(R.string.dither_75, 12),
    Shade50(R.string.dither_50, 8),
    Shade25(R.string.dither_25, 4),
    Shade12(R.string.dither_12, 2),

    Crosshatch(R.string.dither_crosshatch, 6),

    Dots(R.string.dither_dots, 1),
    Diagonal(R.string.dither_diagonal, 8),
    ReverseDiagonal(R.string.dither_reverse_diagonal, 8),
    Horizontal(R.string.dither_horizontal, 8),
    Vertical(R.string.dither_vertical, 8);


    fun usesForeground(row: Int, col: Int): Boolean = when (this) {
        Solid -> true
        Diagonal -> ((row + col) and 3) < 2
        ReverseDiagonal -> ((row - col) and 3) < 2
        Horizontal -> (row and 1) == 0
        Vertical -> (col and 1) == 0
        Crosshatch -> ((row + col) and 3) == 0 || ((row - col) and 3) == 0
        Dots -> (row and 3) == 0 && (col and 3) == 0
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
