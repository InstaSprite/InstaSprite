package com.olaz.instasprite.data.network.model

import com.google.gson.annotations.SerializedName


data class CreateCommentResponseDto(
    @SerializedName("comment")
    val comment: CommentDto
)

