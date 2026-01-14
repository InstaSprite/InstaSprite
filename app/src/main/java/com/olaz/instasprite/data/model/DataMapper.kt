package com.olaz.instasprite.data.model

import com.olaz.instasprite.domain.model.ColorPaletteModel


fun ColorPaletteData.toDomain() : ColorPaletteModel {
    return ColorPaletteModel(
        id = this.id,
        name = this.name,
        author = this.author,
        colors = this.colors
    )
}

fun ColorPaletteModel.toData() : ColorPaletteData {
    return ColorPaletteData(
        id = this.id,
        name = this.name,
        author = this.author,
        colors = this.colors
    )
}