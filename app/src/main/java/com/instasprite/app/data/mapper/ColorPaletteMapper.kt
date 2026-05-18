package com.instasprite.app.data.mapper

import com.instasprite.app.data.model.ColorPaletteData
import com.instasprite.app.domain.model.ColorPalette


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