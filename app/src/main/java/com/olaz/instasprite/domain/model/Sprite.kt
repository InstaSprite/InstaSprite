package com.olaz.instasprite.domain.model

data class Sprite(
    val id: String = "",
    val width: Int,
    val height: Int,
    val pixelsData: List<Int>,
    val colorPalette: List<Int>? = null
)
