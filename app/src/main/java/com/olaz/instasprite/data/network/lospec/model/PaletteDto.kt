package com.olaz.instasprite.data.network.lospec.model

import com.google.gson.annotations.SerializedName
import com.olaz.instasprite.domain.model.ColorPaletteModel
import com.olaz.instasprite.utils.convertHexToColor

class PaletteDto {
    @SerializedName("name")
    val name: String = "";

    @SerializedName("author")
    val author: String = ""

    @SerializedName("colors")
    val colors: List<String> = emptyList()
}

fun PaletteDto.toDomain(): ColorPaletteModel {
    return ColorPaletteModel(
        name = this.name,
        author = this.author,
        colors = this.colors.map { convertHexToColor(it) }.toMutableList(),
    )
}