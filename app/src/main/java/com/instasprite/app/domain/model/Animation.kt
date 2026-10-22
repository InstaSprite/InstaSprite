package com.instasprite.app.domain.model

import java.util.UUID

data class AnimationFrame(
    val id: String = UUID.randomUUID().toString(),
    val durationMs: Int = 100,
    val clippingLayerIds: Set<String> = emptySet()
) {
    init {
        require(id.isNotBlank())
        require(durationMs in 1..60_000)
        require(clippingLayerIds.all { it.isNotBlank() })
    }
}

data class AnimationCel(
    val id: String = UUID.randomUUID().toString(),
    val layerId: String,
    val frameId: String,
    val dataId: String
)

class CelImage(val id: String = UUID.randomUUID().toString(), val tiles: Map<TileCoord, IntArray>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is CelImage && id == other.id && tiles.size == other.tiles.size &&
            tiles.all { (coord, pixels) -> other.tiles[coord]?.contentEquals(pixels) == true }
    }

    override fun hashCode(): Int = id.hashCode()
}

data class Animation(
    val frames: List<AnimationFrame> = listOf(AnimationFrame()),
    val cels: List<AnimationCel> = emptyList(),
    val images: Map<String, CelImage> = emptyMap(),
    val posterFrameId: String = frames.first().id
) {
    fun validated(): Animation {
        require(frames.isNotEmpty() && frames.size <= MAX_FRAMES)
        require(frames.map { it.id }.distinct().size == frames.size)
        require(frames.any { it.id == posterFrameId })
        require(cels.map { it.id }.distinct().size == cels.size)
        require(cels.map { it.layerId to it.frameId }.distinct().size == cels.size)
        val frameIds = frames.map { it.id }.toSet()
        require(cels.all { it.frameId in frameIds && it.dataId in images })
        require(images.all { (id, image) -> id.isNotBlank() && id == image.id })
        require(cels.all { it.id.isNotBlank() && it.layerId.isNotBlank() })
        require(cels.groupBy { it.dataId }.values.all { group -> group.map { it.layerId }.distinct().size == 1 })
        return this
    }

    fun cel(layerId: String, frameId: String): AnimationCel? =
        cels.firstOrNull { it.layerId == layerId && it.frameId == frameId }

    fun tiles(layerId: String, frameId: String): Map<TileCoord, IntArray> =
        cel(layerId, frameId)?.let { images[it.dataId]?.tiles } ?: emptyMap()

    companion object {
        const val MAX_FRAMES = 1000

        fun fromLayers(layers: List<Layer>): Animation {
            val frame = AnimationFrame(clippingLayerIds = layers.filter { it.isClipping }.map { it.id }.toSet())
            val images = layers.filter { it.tiles.isNotEmpty() }.associate { layer ->
                val image = CelImage(tiles = layer.tiles)
                layer.id to image
            }
            return Animation(
                frames = listOf(frame),
                cels = images.map { (layerId, image) ->
                    AnimationCel(layerId = layerId, frameId = frame.id, dataId = image.id)
                },
                images = images.values.associateBy { it.id }
            )
        }
    }
}
