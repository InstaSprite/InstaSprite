package com.olaz.instasprite.domain.model

class Layer(
    val id: String,
    val name: String,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val cel: Cel
) {
    constructor(
        id: String,
        name: String,
        isVisible: Boolean = true,
        isLocked: Boolean = false,
        pixels: IntArray,
        canvasWidth: Int,
        canvasHeight: Int
    ) : this(
        id = id,
        name = name,
        isVisible = isVisible,
        isLocked = isLocked,
        cel = Cel(
            x = 0,
            y = 0,
            width = canvasWidth,
            height = canvasHeight,
            pixels = pixels.copyOf()
        )
    )

    fun copy(
        id: String = this.id,
        name: String = this.name,
        isVisible: Boolean = this.isVisible,
        isLocked: Boolean = this.isLocked,
        cel: Cel? = null
    ): Layer {
        val nextCel = (cel ?: this.cel).copy(pixels = (cel ?: this.cel).pixels.copyOf())
        return Layer(id, name, isVisible, isLocked, nextCel)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Layer) return false
        return id == other.id &&
                name == other.name &&
                isVisible == other.isVisible &&
                isLocked == other.isLocked &&
                cel == other.cel
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + isVisible.hashCode()
        result = 31 * result + isLocked.hashCode()
        result = 31 * result + cel.hashCode()
        return result
    }

    override fun toString(): String =
        "Layer(id='$id', name='$name', isVisible=$isVisible, isLocked=$isLocked, pixels=${cel.pixels.size} items, cel=$cel)"
}
