package com.instasprite.app.data.model

import androidx.room.Entity

@Entity(tableName = "feed_post_cross_ref", primaryKeys = ["postId", "pageFilter"])
data class FeedPostCrossRef(
    val postId: Long,
    val pageFilter: String
)
