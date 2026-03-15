package com.olaz.instasprite.data.network.model

import com.google.gson.annotations.SerializedName

data class PageDto (
    @SerializedName("content")
    val content: List<PostDto>,

    @SerializedName("nextCursor")
    val nextCursor: Long?,

    @SerializedName("hasNext")
    val hasNext: Boolean
)