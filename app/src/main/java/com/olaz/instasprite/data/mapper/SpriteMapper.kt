package com.olaz.instasprite.data.mapper

import com.olaz.instasprite.data.model.SpriteData
import com.olaz.instasprite.data.model.SpriteWithMetaData
import com.olaz.instasprite.data.model.SpriteMetaData as EntitySpriteMetaData
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.domain.model.SpriteMeta as DomainSpriteMetadata
import com.olaz.instasprite.domain.model.SpriteWithMeta

fun SpriteData.toDomain(): Sprite {
    return Sprite(
        id = this.id,
        width = this.width,
        height = this.height,
        pixelsData = this.pixelsData,
        colorPalette = this.colorPalette
    )
}

fun Sprite.toEntity(): SpriteData {
    return SpriteData(
        id = this.id,
        width = this.width,
        height = this.height,
        pixelsData = this.pixelsData,
        colorPalette = this.colorPalette
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
