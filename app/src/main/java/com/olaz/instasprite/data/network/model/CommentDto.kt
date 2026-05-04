package com.olaz.instasprite.data.network.model

import com.google.gson.annotations.SerializedName

data class CommentDto(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("uploadDate")
    val uploadDate: String,  // ISO-8601 string
    
    @SerializedName("member")
    val member: MemberDto,
    
    @SerializedName("commentLikesCount")
    val commentLikesCount: Int? = null,
    
    @SerializedName("commentLikeFlag")
    val commentLikeFlag: Boolean? = null,
    
    @SerializedName("repliesCount")
    val repliesCount: Int? = null,
    
    @SerializedName("mentionsOfContent")
    val mentionsOfContent: List<String>? = null,
    
    @SerializedName("hashtagsOfContent")
    val hashtagsOfContent: List<String>? = null
)

