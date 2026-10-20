package com.instasprite.app.ui.drawing.timeline

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.instasprite.app.domain.model.Animation
import com.instasprite.app.domain.model.AnimationCel
import com.instasprite.app.domain.model.AnimationFrame
import com.instasprite.app.domain.model.CelImage
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.TileCoord

internal object TimelinePreviewData {
    val layers = listOf(
        Layer("background", "Background", tiles = emptyMap()),
        Layer("character", "Character", tiles = emptyMap()),
        Layer("effects", "Effects", isVisible = false, isLocked = true, tiles = emptyMap())
    )
    private val frames = List(12) { AnimationFrame("frame-$it", if (it == 2) 180 else 100) }
    private val images = listOf("background-data", "pose-a", "pose-b", "spark").associateWith {
        CelImage(it, mapOf(TileCoord(0, 0) to IntArray(256) { 0xffa6adc8.toInt() }))
    }
    val animation = Animation(frames = frames, images = images, cels = buildList {
        frames.forEachIndexed { index, frame ->
            add(AnimationCel("background-$index", "background", frame.id, "background-data"))
            if (index in 0..3 || index in 6..9) {
                add(
                    AnimationCel(
                        "character-$index",
                        "character",
                        frame.id,
                        if (index < 4) "pose-a" else "pose-b"
                    )
                )
            }
            if (index == 2 || index == 8) add(
                AnimationCel(
                    "effects-$index",
                    "effects",
                    frame.id,
                    "spark"
                )
            )
        }
    })
}

internal class TimelinePreviewProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}
