package com.olaz.instasprite.domain.model

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
@TypeParceler<Color, ColorParceler>
data class ColorPalette(
    val id: Int = 0,
    val name: String = "Unnamed",
    val author: String = "Anonymous",
    var colors: MutableList<Color> = mutableListOf(),
) : Parcelable

object ColorParceler : Parceler<Color> {
    override fun create(parcel: Parcel): Color {
        return Color(parcel.readInt())
    }

    override fun Color.write(parcel: Parcel, flags: Int) {
        parcel.writeInt(this.toArgb())
    }
}