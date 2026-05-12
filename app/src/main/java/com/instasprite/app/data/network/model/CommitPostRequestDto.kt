package com.instasprite.app.data.network.model

data class CommitPostRequestDto(
    val postId: Long,
    val content: String,
    val commentFlag: Boolean,
    val altTexts: List<String>,
    val hashtags: List<String>
)
