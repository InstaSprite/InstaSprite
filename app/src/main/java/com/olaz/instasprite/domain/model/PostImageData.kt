package com.olaz.instasprite.domain.model

data class PostImageData(
    val id: Long,
    val postImageUrl: String,
    val altText: String,
    val postTags: List<PostTagData> = emptyList(),
    val imageWidth: Int = 1080,
    val imageHeight: Int = 1080
)

