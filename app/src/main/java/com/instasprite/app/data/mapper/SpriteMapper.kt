package com.instasprite.app.data.mapper

import com.instasprite.app.data.model.SpriteData
import com.instasprite.app.data.model.SpriteWithMetaData
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.model.SpriteWithMeta
import com.instasprite.app.data.model.SpriteMetaData as EntitySpriteMetaData
import com.instasprite.app.domain.model.SpriteMeta as DomainSpriteMetadata

fun SpriteData.toDomain(
    layers: List<Layer> = emptyList(),
    colorPalette: List<Int>? = null
): Sprite {
    return Sprite(
        id = this.id,
        width = this.width,
        height = this.height,
        layers = layers,
        colorPalette = colorPalette
    )
}

fun Sprite.toEntity(): SpriteData {
    return SpriteData(
        id = this.id,
        width = this.width,
        height = this.height
    )
}

fun EntitySpriteMetaData.toDomain(): DomainSpriteMetadata {
    return DomainSpriteMetadata(
        spriteId = this.spriteId,
        spriteName = this.spriteName,
        createdAt = this.createdAt,
        lastModifiedAt = this.lastModifiedAt
    )
}

fun DomainSpriteMetadata.toEntity(): EntitySpriteMetaData {
    return EntitySpriteMetaData(
        spriteId = this.spriteId,
        spriteName = this.spriteName,
        createdAt = this.createdAt,
        lastModifiedAt = this.lastModifiedAt
    )
}

fun SpriteWithMetaData.toDomain(): SpriteWithMeta {
    return SpriteWithMeta(
        sprite = this.sprite.toDomain(),
        meta = this.meta?.toDomain()
    )
}

fun SpriteWithMetaData.toDomain(sprite: Sprite): SpriteWithMeta {
    return SpriteWithMeta(
        sprite = sprite,
        meta = this.meta?.toDomain()
    )
}
