package com.olaz.instasprite.domain.model

import java.time.LocalDateTime

data class PostData(
    val postId: Long,
    val postContent: String,
    val mentionsOfContent: List<String> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val postImages: List<PostImageData> = emptyList(),
    val postUploadDate: LocalDateTime,
    val member: MemberData,
    val postCommentsCount: Long,
    val postLikesCount: Long,
    val postBookmarkFlag: Boolean,
    val postLikeFlag: Boolean,
    val commentOptionFlag: Boolean,
    val isFollowing: Boolean,
    val followingMemberUsernameLikedPost: String = "",
    val recentComments: List<CommentData> = emptyList()
)

