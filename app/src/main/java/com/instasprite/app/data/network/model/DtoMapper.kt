package com.instasprite.app.data.network.model

import androidx.core.graphics.toColorInt
import com.instasprite.app.domain.model.CommentData
import com.instasprite.app.domain.model.Jwt
import com.instasprite.app.domain.model.MemberData
import com.instasprite.app.domain.model.MemberImageData
import com.instasprite.app.domain.model.PageData
import com.instasprite.app.domain.model.PostData
import com.instasprite.app.domain.model.PostImageData
import com.instasprite.app.domain.model.PostTagData
import com.instasprite.app.utils.Constants
import com.instasprite.app.domain.model.NotificationData
import com.instasprite.app.domain.model.NotificationType
import com.instasprite.app.data.network.model.NotificationDto
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
    val fullImageUrl = if (imageUrl.startsWith("http")) imageUrl
        else "${Constants.IMG_URL}/$imageUrl"
    return MemberImageData(fullImageUrl)
}

fun PostImageDto.toDomain(): PostImageData {
    val fullImageUrl = if (postImageUrl.startsWith("http")) postImageUrl
        else "${Constants.IMG_URL}/$postImageUrl"
    return PostImageData(
        id = id,
        postImageUrl = fullImageUrl,
        altText = altText ?: "",
        postTags = postTags?.map { it.toDomain() } ?: emptyList(),
        imageWidth = imageWidth ?: 1080,
        imageHeight = imageHeight ?: 1080,
        dominantColor = dominantColor?.toColorInt() ?: 0
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

fun NotificationDto.toDomain(): NotificationData {
    val fullImageUrl = if (senderAvatarUrl?.startsWith("http") == true) {
        senderAvatarUrl
    } else if (!senderAvatarUrl.isNullOrEmpty()) {
        "${Constants.IMG_URL}/$senderAvatarUrl"
    } else {
        null
    }

    return NotificationData(
        id = id,
        title = title,
        body = body,
        type = NotificationType.fromString(type),
        relatedEntityId = relatedEntityId,
        isRead = isRead,
        createdAt = parseDateTime(createdAt),
        senderName = senderName,
        senderUsername = senderUsername,
        senderAvatarUrl = fullImageUrl
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

