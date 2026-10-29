package com.instasprite.app.domain.model


data class Sprite(
    val id: String = "",
    val width: Int,
    val height: Int,
    val layers: List<Layer>,
    val colorPalette: List<Int>? = null,
    val animation: Animation? = null
) {
    fun layersAt(frameId: String): List<Layer> = animation?.let { document ->
        val clippingLayerIds =
            document.frames.firstOrNull { it.id == frameId }?.clippingLayerIds.orEmpty()
        val tilesByLayer = document.cels.filter { it.frameId == frameId }.associate { cel ->
            cel.layerId to document.images[cel.dataId]?.tiles.orEmpty()
        }
        layers.map {
            Layer(
                it.id, it.name, it.isVisible, it.isLocked, it.opacity, it.blendMode,
                tilesByLayer[it.id].orEmpty(), it.id in clippingLayerIds
            )
        }
    } ?: layers

    fun renderFrame(
        frameId: String,
        pixels: IntArray = IntArray(Math.multiplyExact(width, height))
    ): IntArray {
        require(width >= 0 && height >= 0 && pixels.size.toLong() == width.toLong() * height)
        LayerCompositor.render(layersAt(frameId), width, height, pixels)
        return pixels
    }

    val compositedPixels: IntArray
        get() = renderFrame(animation?.posterFrameId.orEmpty())

    override fun toString(): String {
        return "Sprite(id='$id', width=$width, height=$height, layers=${layers.size}, colorPaletteSize=${colorPalette?.size ?: 0})"
    }
}
