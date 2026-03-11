package com.olaz.instasprite.domain.model

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import com.olaz.instasprite.utils.ColorParceler
import com.olaz.instasprite.utils.ColorSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
@TypeParceler<Color, ColorParceler>
data class ColorPalette(
    val id: Int = 0,
    val name: String = "Unnamed",
    val author: String = "Anonymous",
    var colors: MutableList<@Serializable(with = ColorSerializer::class) Color> = mutableListOf(),
) : Parcelable