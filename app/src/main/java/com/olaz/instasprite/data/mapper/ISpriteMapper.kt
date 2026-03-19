package com.olaz.instasprite.data.mapper

import com.google.protobuf.ByteString
import com.olaz.instasprite.ISprite
import com.olaz.instasprite.LayerData
import com.olaz.instasprite.domain.model.Layer
import com.olaz.instasprite.domain.model.Sprite
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun Sprite.toISprite(): ISprite {
    val builder = ISprite.newBuilder()
        .setWidth(this.width)
        .setHeight(this.height)

    val layerProtos = this.layers.map { layer ->
        val byteBuffer = ByteBuffer.allocate(layer.pixels.size * 4)

        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.asIntBuffer().put(layer.pixels)

        LayerData.newBuilder()
            .setId(layer.id)
            .setName(layer.name)
            .setIsVisible(layer.isVisible)
            .setIsLocked(layer.isLocked)
            .setPixels(ByteString.copyFrom(byteBuffer.array()))
            .build()
    }

    builder.addAllLayers(layerProtos)

    this.colorPalette?.let { builder.addAllColorPalette(it) }
    return builder.build()
}

fun ISprite.toSprite(): Sprite {
    val layers = this.layersList.map { layerData ->
        val byteString = layerData.pixels
        val byteBuffer = byteString.asReadOnlyByteBuffer()

        val intArray = IntArray(byteString.size() / 4)

        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.asIntBuffer().get(intArray)

        Layer(
            id = layerData.id,
            name = layerData.name,
            isVisible = layerData.isVisible,
            isLocked = layerData.isLocked,
            pixels = intArray
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