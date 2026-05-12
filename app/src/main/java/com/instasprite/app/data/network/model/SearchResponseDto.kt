package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName

data class SearchResponseDto(
    @SerializedName("type")
    val type: String, // "MEMBER", "POST", "HASHTAG"

    @SerializedName("members")
    val members: List<MemberDto>?,

    @SerializedName("posts")
    val posts: List<PostDto>?,

    @SerializedName("hashtag")
    val hashtag: String?,

    @SerializedName("totalCount")
    val totalCount: Long,

    @SerializedName("hasNext")
    val hasNext: Boolean,

    @SerializedName("page")
    val page: Int,

    @SerializedName("size")
    val size: Int
)
