package com.instasprite.app.data.mapper

import com.google.protobuf.ByteString
import com.instasprite.app.AnimationCelData
import com.instasprite.app.AnimationFrameData
import com.instasprite.app.AnimationImageData
import com.instasprite.app.CelData
import com.instasprite.app.ISprite
import com.instasprite.app.LayerData
import com.instasprite.app.domain.model.Animation
import com.instasprite.app.domain.model.AnimationCel
import com.instasprite.app.domain.model.AnimationFrame
import com.instasprite.app.domain.model.BlendMode
import com.instasprite.app.domain.model.Cel
import com.instasprite.app.domain.model.CelImage
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.utils.celToTiles
import com.instasprite.app.utils.decodeTilesFromByteArray
import com.instasprite.app.utils.encodeTilesToByteArray
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
        val layerBuilder = LayerData.newBuilder()
            .setId(layer.id)
            .setName(layer.name)
            .setIsVisible(layer.isVisible)
            .setIsLocked(layer.isLocked)
            .setOpacity(layer.opacity)
            .setBlendMode(layer.blendMode.name)

        layerBuilder.build()
    }

    builder.addAllLayers(layerProtos)

    this.colorPalette?.let { builder.addAllColorPalette(it) }
    val document = (animation ?: Animation.fromLayers(layers)).validated()
    builder.setFormatVersion(if (document.frames.any { it.clippingLayerIds.isNotEmpty() }) 3 else 1)
    builder.setPosterFrameId(document.posterFrameId)
    builder.addAllFrames(document.frames.map {
        AnimationFrameData.newBuilder().setId(it.id).setDurationMs(it.durationMs)
            .addAllClippingLayerIds(it.clippingLayerIds).build()
    })
    builder.addAllCels(document.cels.map {
        AnimationCelData.newBuilder().setId(it.id).setLayerId(it.layerId)
            .setFrameId(it.frameId).setDataId(it.dataId).build()
    })
    builder.addAllImages(document.images.values.map {
        AnimationImageData.newBuilder().setId(it.id)
            .setTiles(ByteString.copyFrom(encodeTilesToByteArray(it.tiles))).build()
    })
    return builder.build()
}

fun ISprite.toSprite(): Sprite {
    require(formatVersion in 0..3) { "Unsupported sprite format: $formatVersion" }
    require(width > 0 && height > 0 && width.toLong() * height <= 16_777_216) { "Invalid canvas size" }
    require(framesCount <= Animation.MAX_FRAMES)

    val layers = this.layersList.map { layerData ->
        // Backward compatibility: pb defaults float to 0.0 and string to ""
        val opacity = if (formatVersion >= 1 || layerData.blendMode.isNotEmpty() || layerData.opacity > 0f) layerData.opacity else 1.0f
        val blendMode = try {
            if (layerData.blendMode.isNotEmpty()) BlendMode.valueOf(layerData.blendMode) else BlendMode.NORMAL
        } catch (_: IllegalArgumentException) {
            BlendMode.NORMAL
        }

        Layer(
            id = layerData.id,
            name = layerData.name,
            isVisible = layerData.isVisible,
            isLocked = layerData.isLocked,
            opacity = opacity,
            blendMode = blendMode,
            isClipping = layerData.isClipping,
            tiles = when {
                layerData.hasCel() -> {
                    val decodedTiles = decodeTilesFromByteArray(layerData.cel.pixels.toByteArray())
                    decodedTiles ?: celToTiles(layerData.cel.toDomain())
                }

                !layerData.pixels.isEmpty -> celToTiles(
                    Cel(
                        x = 0,
                        y = 0,
                        width = width,
                        height = height,
                        pixels = layerData.pixels.toIntArray()
                    )
                )

                else -> emptyMap()
            }
        )
    }
    return Sprite(
        id = "", // Prolly used when load from file so generates empty ID; app creates unique DB ID on save
        width = this.width,
        height = this.height,
        layers = layers,
        colorPalette = this.colorPaletteList.takeIf { it.isNotEmpty() },
        animation = if (formatVersion == 0) Animation.fromLayers(layers) else Animation(
            frames = framesList.map {
                AnimationFrame(it.id, it.durationMs, if (formatVersion >= 3) it.clippingLayerIdsList.toSet()
                    else layers.filter { layer -> layer.isClipping }.map { layer -> layer.id }.toSet())
            },
            cels = celsList.map { AnimationCel(it.id, it.layerId, it.frameId, it.dataId) },
            images = imagesList.map {
                CelImage(it.id, requireNotNull(decodeTilesFromByteArray(it.tiles.toByteArray())))
            }.also { images -> require(images.map { it.id }.distinct().size == images.size) }
                .associateBy { it.id },
            posterFrameId = posterFrameId
        ).validated().also { animation ->
            require(layers.map { it.id }.distinct().size == layers.size)
            require(animation.cels.all { cel -> layers.any { it.id == cel.layerId } })
            val layerIds = layers.map { it.id }.toSet()
            require(animation.frames.all { layerIds.containsAll(it.clippingLayerIds) })
        }
    )
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
