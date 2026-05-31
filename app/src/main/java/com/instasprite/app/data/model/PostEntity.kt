package com.instasprite.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instasprite.app.data.network.model.CommentDto
import com.instasprite.app.data.network.model.PostImageDto

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val postId: Long,
    val authorId: Long,
    val postContent: String?,
    val postUploadDate: String?,
    val postCommentsCount: Long,
    val postLikesCount: Long,
    val postBookmarkFlag: Boolean,
    val postLikeFlag: Boolean,
    val commentOptionFlag: Boolean,
    val likeOptionFlag: Boolean,
    val isFollowing: Boolean?,
    val followingMemberUsernameLikedPost: String?,
    val mentionsOfContent: List<String>?,
    val hashtags: List<String>?,
    val postImages: List<PostImageDto>?,
    val recentComments: List<CommentDto>?
)
