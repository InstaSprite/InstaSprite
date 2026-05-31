package com.instasprite.app.data.network.model

import androidx.core.graphics.toColorInt
import com.instasprite.app.data.model.NotificationEntity
import com.instasprite.app.data.model.PostEntity
import com.instasprite.app.data.model.UserEntity
import com.instasprite.app.data.model.UserProfileEntity
import com.instasprite.app.domain.model.CommentData
import com.instasprite.app.domain.model.GroupedNotificationData
import com.instasprite.app.domain.model.Jwt
import com.instasprite.app.domain.model.MemberData
import com.instasprite.app.domain.model.MemberImageData
import com.instasprite.app.domain.model.NotificationData
import com.instasprite.app.domain.model.NotificationType
import com.instasprite.app.domain.model.PageData
import com.instasprite.app.domain.model.PostData
import com.instasprite.app.domain.model.PostImageData
import com.instasprite.app.domain.model.PostTagData
import com.instasprite.app.utils.Constants
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

fun GroupedNotificationDto.toDomain(): GroupedNotificationData {
    return GroupedNotificationData(
        id = id,
        groupKey = groupKey,
        type = NotificationType.fromString(type),
        relatedEntityId = relatedEntityId,
        actorCount = actorCount,
        isRead = isRead,
        updatedAt = parseDateTime(updatedAt),
        recentActors = recentActors.map { actor ->
            val fullImageUrl = if (actor.avatarUrl?.startsWith("http") == true) {
                actor.avatarUrl
            } else if (!actor.avatarUrl.isNullOrEmpty()) {
                "${Constants.IMG_URL}/${actor.avatarUrl}"
            } else {
                null
            }
            GroupedNotificationData.ActorSummary(
                name = actor.name,
                username = actor.username,
                avatarUrl = fullImageUrl
            )
        },
        title = title,
        body = body
    )
}


fun PostDto.toPostEntity(fallbackAuthorId: Long = 0L): PostEntity {
    return PostEntity(
        postId = postId,
        authorId = member?.id ?: fallbackAuthorId,
        postContent = postContent,
        postUploadDate = postUploadDate,
        postCommentsCount = postCommentsCount,
        postLikesCount = postLikesCount,
        postBookmarkFlag = postBookmarkFlag,
        postLikeFlag = postLikeFlag,
        commentOptionFlag = commentOptionFlag,
        likeOptionFlag = likeOptionFlag,
        isFollowing = following ?: isFollowing,
        followingMemberUsernameLikedPost = followingMemberUsernameLikedPost,
        mentionsOfContent = mentionsOfContent,
        hashtags = hashtags,
        postImages = postImages ?: postImage?.let { listOf(it) },
        recentComments = recentComments
    )
}

fun MemberDto.toUserEntity(): UserEntity {
    return UserEntity(
        memberId = id,
        username = username,
        name = name,
        avatarUrl = image?.imageUrl
    )
}

fun com.instasprite.app.data.model.PostWithAuthor.toDomain(): PostData {
    val authorData = author?.let {
        MemberData(
            memberId = it.memberId,
            memberUsername = it.username,
            memberName = it.name,
            memberImage = if (it.avatarUrl.isNullOrEmpty()) null else com.instasprite.app.domain.model.MemberImageData(
                imageUrl = if (it.avatarUrl.startsWith("http")) it.avatarUrl 
                           else "${com.instasprite.app.utils.Constants.IMG_URL}/${it.avatarUrl}"
            )
        )
    } ?: MemberData(0, "Unknown", "Unknown", null)

    return PostData(
        postId = post.postId,
        postContent = post.postContent ?: "",
        mentionsOfContent = post.mentionsOfContent ?: emptyList(),
        hashtags = post.hashtags ?: emptyList(),
        postImages = post.postImages?.map { it.toDomain() } ?: emptyList(),
        postUploadDate = parseDateTime(post.postUploadDate),
        member = authorData,
        postCommentsCount = post.postCommentsCount,
        postLikesCount = post.postLikesCount,
        postBookmarkFlag = post.postBookmarkFlag,
        postLikeFlag = post.postLikeFlag,
        commentOptionFlag = post.commentOptionFlag,
        isFollowing = post.isFollowing ?: false,
        followingMemberUsernameLikedPost = post.followingMemberUsernameLikedPost ?: "",
        recentComments = post.recentComments?.map { it.toDomain() } ?: emptyList()
    )
}

fun NotificationEntity.toDomain(): GroupedNotificationData {
    return GroupedNotificationData(
        id = id,
        groupKey = groupKey,
        type = NotificationType.fromString(type),
        relatedEntityId = relatedEntityId,
        actorCount = actorCount,
        isRead = isRead,
        updatedAt = parseDateTime(updatedAt),
        recentActors = recentActors.map { actor ->
            val fullImageUrl = if (actor.avatarUrl?.startsWith("http") == true) {
                actor.avatarUrl
            } else if (!actor.avatarUrl.isNullOrEmpty()) {
                "${Constants.IMG_URL}/${actor.avatarUrl}"
            } else {
                null
            }
            GroupedNotificationData.ActorSummary(
                name = actor.name,
                username = actor.username,
                avatarUrl = fullImageUrl
            )
        },
        title = title,
        body = body
    )
}

fun UserProfileDto.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        memberId = memberId,
        memberUsername = memberUsername,
        memberName = memberName,
        memberImage = memberImage,
        memberImageUrl = memberImageUrl,
        memberIntroduce = memberIntroduce,
        memberPostsCount = memberPostsCount,
        memberFollowingsCount = memberFollowingsCount,
        memberFollowersCount = memberFollowersCount,
        followingMemberFollow = followingMemberFollow,
        followingMemberFollowCount = followingMemberFollowCount,
        blocking = blocking,
        following = following,
        me = me,
        follower = follower,
        blocked = blocked,
        verifiedEmail = verifiedEmail,
        hasPassword = hasPassword
    )
}

fun UserProfileEntity.toDto(): UserProfileDto {
    return UserProfileDto(
        memberId = memberId,
        memberUsername = memberUsername,
        memberName = memberName,
        memberImage = memberImage,
        memberImageUrl = memberImageUrl,
        memberIntroduce = memberIntroduce,
        memberPostsCount = memberPostsCount,
        memberFollowingsCount = memberFollowingsCount,
        memberFollowersCount = memberFollowersCount,
        followingMemberFollow = followingMemberFollow,
        followingMemberFollowCount = followingMemberFollowCount,
        blocking = blocking,
        following = following,
        me = me,
        follower = follower,
        blocked = blocked,
        verifiedEmail = verifiedEmail,
        hasPassword = hasPassword
    )
}
