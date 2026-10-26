package com.instasprite.app.domain.export

enum class AnimationExportFormat(val extension: String, val mimeType: String) {
    GIF("gif", "image/gif"),
    SPRITE_SHEET("png", "image/png")
}
