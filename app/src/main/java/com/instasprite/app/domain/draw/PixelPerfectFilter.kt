package com.instasprite.app.domain.draw

import kotlin.math.abs

class PixelPerfectFilter {
    private var pendingRow = 0
    private var pendingCol = 0
    private var hasPending = false

    private var prevRow = 0
    private var prevCol = 0
    private var hasPrev = false

    fun start(row: Int, col: Int) {
        pendingRow = row
        pendingCol = col
        hasPending = true
        hasPrev = false
    }

    fun addPoint(row: Int, col: Int, onPixelEmitted: (row: Int, col: Int) -> Unit) {
        if (hasPending && row == pendingRow && col == pendingCol) return

        if (!hasPending) {
            pendingRow = row
            pendingCol = col
            hasPending = true
            return
        }

        if (!hasPrev) {
            onPixelEmitted(pendingRow, pendingCol)
            prevRow = pendingRow
            prevCol = pendingCol
            hasPrev = true
            pendingRow = row
            pendingCol = col
            return
        }

        if (isLShape(prevRow, prevCol, pendingRow, pendingCol, row, col)) {
            pendingRow = row
            pendingCol = col
        } else {
            onPixelEmitted(pendingRow, pendingCol)
            prevRow = pendingRow
            prevCol = pendingCol
            pendingRow = row
            pendingCol = col
        }
    }

    fun end(onPixelEmitted: (row: Int, col: Int) -> Unit) {
        if (hasPending) {
            onPixelEmitted(pendingRow, pendingCol)
        }
        clear()
    }

    fun clear() {
        hasPending = false
        hasPrev = false
    }

    private fun isLShape(
        r1: Int, c1: Int,
        r2: Int, c2: Int,
        r3: Int, c3: Int
    ): Boolean {
        val abDist = abs(r1 - r2) + abs(c1 - c2)
        val bcDist = abs(r2 - r3) + abs(c2 - c3)
        val acDr = abs(r1 - r3)
        val acDc = abs(c1 - c3)
        return abDist == 1 && bcDist == 1 && acDr == 1 && acDc == 1
    }
}
