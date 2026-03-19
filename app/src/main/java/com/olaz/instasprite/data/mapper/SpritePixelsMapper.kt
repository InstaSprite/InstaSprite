package com.olaz.instasprite.data.mapper

import com.olaz.instasprite.LayerData
import com.olaz.instasprite.SpritePixels
import com.olaz.instasprite.domain.model.Layer
import com.olaz.instasprite.domain.model.Sprite

fun Sprite.toSpritePixels(): SpritePixels {
    val builder = SpritePixels.newBuilder()
        .setWidth(this.width)
        .setHeight(this.height)

    val layerProtos = this.layers.map { layer ->
        LayerData.newBuilder()
            .setId(layer.id)
            .setName(layer.name)
            .setIsVisible(layer.isVisible)
            .setIsLocked(layer.isLocked)
            .addAllPixels(layer.pixels.toList())
            .build()
    }
    builder.addAllLayers(layerProtos)

    this.colorPalette?.let { builder.addAllColorPalette(it) }
    return builder.build()
}

fun SpritePixels.toSprite(): Sprite {
    val layers = this.layersList.map { layerData ->
        Layer(
            id = layerData.id,
            name = layerData.name,
            isVisible = layerData.isVisible,
            isLocked = layerData.isLocked,
            pixels = layerData.pixelsList.toIntArray()
        )
    }
    return Sprite(
        id = "", // Prolly used when load from file so generates empty ID; app creates unique DB ID on save
        width = this.width,
        height = this.height,
        layers = layers,
        colorPalette = this.colorPaletteList.takeIf { it.isNotEmpty() }
    )
}