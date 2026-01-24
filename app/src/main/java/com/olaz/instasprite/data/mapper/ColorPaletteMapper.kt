package com.olaz.instasprite.data.mapper

import com.olaz.instasprite.data.model.ColorPaletteData
import com.olaz.instasprite.domain.model.ColorPalette


fun ColorPaletteData.toDomain() : ColorPalette {
    return ColorPalette(
        id = this.id,
        name = this.name,
        author = this.author,
        colors = this.colors
    )
}

fun ColorPalette.toData() : ColorPaletteData {
    return ColorPaletteData(
        id = this.id,
        name = this.name,
        author = this.author,
        colors = this.colors
    )
}