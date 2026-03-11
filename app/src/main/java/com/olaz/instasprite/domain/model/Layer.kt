package com.olaz.instasprite.domain.model

class Layer(
    val id: String,
    val name: String,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val pixels: IntArray
) {
    fun copy(
        id: String = this.id,
        name: String = this.name,
        isVisible: Boolean = this.isVisible,
        isLocked: Boolean = this.isLocked,
        pixels: IntArray = this.pixels
    ): Layer = Layer(id, name, isVisible, isLocked, pixels)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Layer) return false
        return id == other.id &&
                name == other.name &&
                isVisible == other.isVisible &&
                isLocked == other.isLocked &&
                pixels.contentEquals(other.pixels)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + isVisible.hashCode()
        result = 31 * result + isLocked.hashCode()
        result = 31 * result + pixels.contentHashCode()
        return result
    }

    override fun toString(): String =
        "Layer(id='$id', name='$name', isVisible=$isVisible, isLocked=$isLocked, pixels=${pixels.size} items)"
}
