package com.olaz.instasprite.domain.model

class Layer(
    val id: String,
    val name: String,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val pixels: IntArray,
    val cel: Cel? = null
) {
    fun copy(
        id: String = this.id,
        name: String = this.name,
        isVisible: Boolean = this.isVisible,
        isLocked: Boolean = this.isLocked,
        pixels: IntArray = this.pixels,
        cel: Cel? = this.cel
    ): Layer = Layer(id, name, isVisible, isLocked, pixels, cel)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Layer) return false
        return id == other.id &&
                name == other.name &&
                isVisible == other.isVisible &&
                isLocked == other.isLocked &&
                pixels.contentEquals(other.pixels) &&
                cel == other.cel
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + isVisible.hashCode()
        result = 31 * result + isLocked.hashCode()
        result = 31 * result + pixels.contentHashCode()
        result = 31 * result + (cel?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "Layer(id='$id', name='$name', isVisible=$isVisible, isLocked=$isLocked, pixels=${pixels.size} items, cel=$cel)"
}
