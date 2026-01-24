
package com.olaz.instasprite.data.mapper

import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.SpritePixels

fun Sprite.toSpritePixels(): SpritePixels {
    val builder = SpritePixels.newBuilder()
    this.pixelsData?.let { builder.addAllPixels(it) }
    this.colorPalette?.let { builder.addAllColorPalette(it) }
    return builder.build()
}
