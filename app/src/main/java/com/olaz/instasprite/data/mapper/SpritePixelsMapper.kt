package com.olaz.instasprite.data.mapper

import com.olaz.instasprite.LayerData
import com.olaz.instasprite.SpritePixels
import com.olaz.instasprite.domain.model.Sprite

fun Sprite.toSpritePixels(): SpritePixels {
    val builder = SpritePixels.newBuilder()

    val layerProtos = this.layers.map { layer ->
        LayerData.newBuilder()
            .setId(layer.id)
            .setName(layer.name)
            .setIsVisible(layer.isVisible)
            .setIsLocked(layer.isLocked)
            .addAllPixels(layer.pixels)
            .build()
    }
    builder.addAllLayers(layerProtos)

    this.colorPalette?.let { builder.addAllColorPalette(it) }
    return builder.build()
}
