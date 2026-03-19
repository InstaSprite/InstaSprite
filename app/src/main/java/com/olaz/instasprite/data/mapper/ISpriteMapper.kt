package com.olaz.instasprite.data.mapper

import com.google.protobuf.ByteString
import com.olaz.instasprite.CelData
import com.olaz.instasprite.ISprite
import com.olaz.instasprite.LayerData
import com.olaz.instasprite.domain.model.Cel
import com.olaz.instasprite.domain.model.Layer
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.utils.calculateBoundingBox
import com.olaz.instasprite.utils.extractCel
import com.olaz.instasprite.utils.inflateCel
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

fun Sprite.toISprite(): ISprite {
    val builder = ISprite.newBuilder()
        .setWidth(this.width)
        .setHeight(this.height)

    val layerProtos = this.layers.map { layer ->
        val cel = calculateBoundingBox(
            pixels = layer.pixels,
            canvasWidth = width,
            canvasHeight = height
        )?.let { bounds ->
            extractCel(
                pixels = layer.pixels,
                bounds = bounds,
                canvasWidth = width
            )
        }

        val layerBuilder = LayerData.newBuilder()
            .setId(layer.id)
            .setName(layer.name)
            .setIsVisible(layer.isVisible)
            .setIsLocked(layer.isLocked)

        if (cel != null) {
            layerBuilder.setCel(cel.toProto())
        } else if (layer.pixels.isNotEmpty()) {
            layerBuilder.setCel(
                CelData.newBuilder()
                    .setWidth(0)
                    .setHeight(0)
                    .setPixels(ByteString.EMPTY)
                    .build()
            )
        }

        layerBuilder.build()
    }

    builder.addAllLayers(layerProtos)

    this.colorPalette?.let { builder.addAllColorPalette(it) }
    return builder.build()
}

fun ISprite.toSprite(): Sprite {
    val layers = this.layersList.map { layerData ->
        val cel = when {
            layerData.hasCel() -> layerData.cel.toDomain()
            !layerData.pixels.isEmpty -> Cel(
                x = 0,
                y = 0,
                width = width,
                height = height,
                pixels = layerData.pixels.toIntArray()
            )
            else -> null
        }

        val intArray = inflateCel(
            cel = cel,
            canvasWidth = width,
            canvasHeight = height
        )

        Layer(
            id = layerData.id,
            name = layerData.name,
            isVisible = layerData.isVisible,
            isLocked = layerData.isLocked,
            pixels = intArray,
            cel = cel
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

private fun Cel.toProto(): CelData {
    val byteBuffer = ByteBuffer.allocate(pixels.size * Int.SIZE_BYTES)
        .order(ByteOrder.LITTLE_ENDIAN)
    byteBuffer.asIntBuffer().put(pixels)

    return CelData.newBuilder()
        .setX(x)
        .setY(y)
        .setWidth(width)
        .setHeight(height)
        .setPixels(ByteString.copyFrom(gzip(byteBuffer.array())))
        .build()
}

private fun CelData.toDomain(): Cel {
    val pixelBytes = if (pixels.isEmpty) {
        ByteArray(0)
    } else {
        ungzip(pixels.toByteArray())
    }

    val pixelBuffer = ByteBuffer.wrap(pixelBytes).order(ByteOrder.LITTLE_ENDIAN)
    val pixelArray = IntArray(pixelBytes.size / Int.SIZE_BYTES)
    pixelBuffer.asIntBuffer().get(pixelArray)

    return Cel(
        x = x,
        y = y,
        width = width,
        height = height,
        pixels = pixelArray
    )
}

private fun ByteString.toIntArray(): IntArray {
    val byteBuffer = asReadOnlyByteBuffer().order(ByteOrder.LITTLE_ENDIAN)
    val intArray = IntArray(size() / Int.SIZE_BYTES)
    byteBuffer.asIntBuffer().get(intArray)
    return intArray
}

private fun gzip(input: ByteArray): ByteArray {
    val output = ByteArrayOutputStream(input.size)
    GZIPOutputStream(output).use { gzip ->
        gzip.write(input)
        gzip.finish()
    }
    return output.toByteArray()
}

private fun ungzip(input: ByteArray): ByteArray {
    ByteArrayInputStream(input).use { byteStream ->
        GZIPInputStream(byteStream).use { gzip ->
            return gzip.readBytes()
        }
    }
}
