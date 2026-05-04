package com.olaz.instasprite.data.network.model

import com.google.gson.annotations.SerializedName


data class CommentUploadRequestDto(
    @SerializedName("postId")
    val postId: Long,
    
    @SerializedName("parentId")
    val parentId: Long = 0L,
    
    @SerializedName("content")
    val content: String
)

