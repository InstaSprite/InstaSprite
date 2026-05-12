package com.instasprite.app.ui.loadimage.contract

import android.graphics.Bitmap
import com.instasprite.app.domain.image2pixel.PixelArtConfig
import com.instasprite.app.domain.model.ColorPalette

data class LoadImageUiState(
    val sourceBitmap: Bitmap? = null,
    val processedBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val config: PixelArtConfig = PixelArtConfig(),
    val spriteName: String = "Untitled",
    val applyPalette: Boolean = false,
    val selectedPalette: ColorPalette? = null,
    val selectedTabIndex: Int = 0
)

sealed interface ImageConfigEvent {
    data class ApplyPaletteChange(val apply: Boolean) : ImageConfigEvent
    data class SpriteNameChange(val name: String) : ImageConfigEvent
    data class TargetWidthChange(val width: Int) : ImageConfigEvent
    data class ColorCountChange(val count: Int) : ImageConfigEvent
    data class AutoDetectChange(val autoDetect: Boolean) : ImageConfigEvent
    data class DitheringChange(val enabled: Boolean) : ImageConfigEvent
    data class TabSelectionChange(val index: Int) : ImageConfigEvent
}
