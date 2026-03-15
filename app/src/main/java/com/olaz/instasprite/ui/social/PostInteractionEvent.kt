package com.olaz.instasprite.ui.social

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

object PostInteractionEvent {

    val followStateChangeEvents = MutableSharedFlow<Pair<String, Boolean>>(replay = 1)

    val postLikeEvent = MutableSharedFlow<Pair<Long, Boolean>>(replay = 1)
    val postBookmarkEvent = MutableSharedFlow<Pair<Long, Boolean>>(replay = 1)
    val postCommentEvent = MutableSharedFlow<Pair<Long, Long>>(replay = 1)
    val postCreatedEvent = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val emailVerificationEvent = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    suspend fun emitFollowEvent(username: String, isFollowing: Boolean) {
        followStateChangeEvents.emit(username to isFollowing)
    }

    suspend fun emitLikeEvent(postId: Long, isLiked: Boolean) {
        postLikeEvent.emit(postId to isLiked)
    }

    suspend fun emitBookmarkEvent(postId: Long, isBookmarked: Boolean) {
        postBookmarkEvent.emit(postId to isBookmarked)
    }

    suspend fun emitCommentEvent(postId: Long, delta: Long) {
        postCommentEvent.emit(postId to delta)
    }

    suspend fun emitPostCreated() {
        postCreatedEvent.emit(Unit)
    }

    suspend fun emitEmailVerificationEvent() {
        emailVerificationEvent.emit(Unit)
    }

    val profileRefreshEvent = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    suspend fun emitProfileRefreshEvent() {
        profileRefreshEvent.emit(Unit)
    }
}