package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName


data class PostDto(
    @SerializedName("postId")
    val postId: Long,
    
    @SerializedName("postContent")
    val postContent: String? = null,
    
    @SerializedName("postUploadDate")
    val postUploadDate: String? = null,  // ISO-8601 string, will be parsed in mapper
    
    @SerializedName("member")
    val member: MemberDto? = null,
    
    @SerializedName("postCommentsCount")
    val postCommentsCount: Long = 0,
    
    @SerializedName("postLikesCount")
    val postLikesCount: Long = 0,
    
    @SerializedName("postBookmarkFlag")
    val postBookmarkFlag: Boolean = false,
    
    @SerializedName("postLikeFlag")
    val postLikeFlag: Boolean = false,
    
    @SerializedName("commentOptionFlag")
    val commentOptionFlag: Boolean = true,
    
    @SerializedName("likeOptionFlag")
    val likeOptionFlag: Boolean = true,
    
    // Handle both "following" and "isFollowing" field names
    @SerializedName("following")
    val following: Boolean? = null,
    
    @SerializedName("isFollowing")
    val isFollowing: Boolean? = null,
    
    @SerializedName("followingMemberUsernameLikedPost")
    val followingMemberUsernameLikedPost: String? = null,
    
    @SerializedName("mentionsOfContent")
    val mentionsOfContent: List<String>? = null,
    
    @SerializedName("hashtags")
    val hashtags: List<String>? = null,
    
    // Handle both "postImages" (array) and "postImage" (single) formats
    @SerializedName("postImages")
    val postImages: List<PostImageDto>? = null,
    
    @SerializedName("postImage")
    val postImage: PostImageDto? = null,
    
    @SerializedName("recentComments")
    val recentComments: List<CommentDto>? = null
)

