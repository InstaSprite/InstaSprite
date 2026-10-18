package com.instasprite.app.domain.draw

import com.instasprite.app.domain.model.AnimationFrame

class AnimationPlayback(private val frames: List<AnimationFrame>, startFrameId: String) {
    private val cycleMs = frames.sumOf { it.durationMs.toLong() }
    private val startOffset = frames.takeWhile { it.id != startFrameId }.sumOf { it.durationMs.toLong() }

    fun frameAt(elapsedMs: Long): String {
        var position = (startOffset + elapsedMs.coerceAtLeast(0) % cycleMs) % cycleMs
        for (frame in frames) {
            if (position < frame.durationMs) return frame.id
            position -= frame.durationMs
        }
        return frames.last().id
    }
}
