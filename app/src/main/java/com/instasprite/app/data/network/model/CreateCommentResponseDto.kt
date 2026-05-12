package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName


data class CreateCommentResponseDto(
    @SerializedName("comment")
    val comment: CommentDto
)

