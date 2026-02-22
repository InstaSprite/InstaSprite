package com.olaz.instasprite.domain.model

data class Layer(
    val id: String,
    val name: String,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val pixels: List<Int>
)
