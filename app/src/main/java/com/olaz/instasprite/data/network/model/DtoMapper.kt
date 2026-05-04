package com.olaz.instasprite.data.network.model

import com.olaz.instasprite.domain.model.CommentData
import com.olaz.instasprite.domain.model.Jwt
import com.olaz.instasprite.domain.model.MemberData
import com.olaz.instasprite.domain.model.MemberImageData
import com.olaz.instasprite.domain.model.PageData
import com.olaz.instasprite.domain.model.PostData
import com.olaz.instasprite.domain.model.PostImageData
import com.olaz.instasprite.domain.model.PostTagData
import com.olaz.instasprite.utils.Constants
import java.time.LocalDateTime

/**
 * Extension functions to map DTOs to Domain models
 */
fun PostDto.toDomain(): PostData {
    return PostData(
        postId = postId,
        postContent = postContent ?: "",
        mentionsOfContent = mentionsOfContent ?: emptyList(),
        hashtags = hashtags ?: emptyList(),
        postImages = getPostImages().map { it.toDomain() },
        postUploadDate = parseDateTime(postUploadDate),
        member = member?.toDomain() ?: MemberData(
            memberId = 0,
            memberUsername = "",
            memberName = "",
            memberImage = null
        ),
        postCommentsCount = postCommentsCount,
        postLikesCount = postLikesCount,
        postBookmarkFlag = postBookmarkFlag,
        postLikeFlag = postLikeFlag,
        commentOptionFlag = commentOptionFlag,
        isFollowing = isFollowing ?: following ?: false,
        followingMemberUsernameLikedPost = followingMemberUsernameLikedPost ?: "",
        recentComments = recentComments?.map { it.toDomain() } ?: emptyList()
    )
}

/**
 * Handle both postImages (array) and postImage (single) formats
 */
private fun PostDto.getPostImages(): List<PostImageDto> {
    return when {
        !postImages.isNullOrEmpty() -> postImages
        postImage != null -> listOf(postImage)
        else -> emptyList()
    }
}

fun MemberDto.toDomain(): MemberData {
    return MemberData(
        memberId = id,
        memberUsername = username,
        memberName = name,
        memberImage = image?.toDomain()
    )
}

fun MemberImageDto.toDomain(): MemberImageData? {
    val imageUrl = imageUrl ?: return null
    val fullImageUrl = "${Constants.BASE_URL}/images/$imageUrl"
    return MemberImageData(fullImageUrl)
}

fun PostImageDto.toDomain(): PostImageData {
    val fullImageUrl = "${Constants.BASE_URL}/images/$postImageUrl"
    return PostImageData(
        id = id,
        postImageUrl = fullImageUrl,
        altText = altText ?: "",
        postTags = postTags?.map { it.toDomain() } ?: emptyList(),
        imageWidth = imageWidth ?: 1080,
        imageHeight = imageHeight ?: 1080
    )
}

fun PostTagDto.toDomain(): PostTagData {
    return PostTagData(
        id = id,
        tagX = tagX,
        tagY = tagY,
        member = member.toDomain()
    )
}

fun PageDto.toDomain(): PageData {
    return PageData(
        content = content.toDomain(),
        nextCursor = nextCursor,
        hasNext = hasNext
    )
}

fun List<PostDto>.toDomain(): List<PostData> {
    return this.map { it.toDomain() }
}

fun CommentDto.toDomain(): CommentData {
    return CommentData(
        id = id,
        member = member.toDomain(),
        content = content,
        uploadDate = parseDateTime(uploadDate),
        commentLikesCount = commentLikesCount ?: 0,
        commentLikeFlag = commentLikeFlag ?: false,
        repliesCount = repliesCount ?: 0,
        mentionsOfContent = mentionsOfContent ?: emptyList(),
        hashtagsOfContent = hashtagsOfContent ?: emptyList()
    )
}

fun List<FollowingDto>.toUsernameSet(): Set<String> {
    return this.mapNotNull { followingResponse ->
        val username = followingResponse.member.username.trim()
        username.ifBlank { null }
    }.toSet()
}


fun JwtDto.toDomain(): Jwt {
    return Jwt(
        type = type,
        accessToken = accessToken,
        refreshToken = refreshToken,
        name = name,
        username = username,
        email = email,
        isFirstTime = isFirstTime
    )
}

private fun parseDateTime(dateString: String?): LocalDateTime {
    if (dateString == null) return LocalDateTime.now()
    return try {
        LocalDateTime.parse(dateString)
    } catch (e: Exception) {
        LocalDateTime.now()
    }
}

