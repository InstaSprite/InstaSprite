package com.instasprite.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "post_remote_keys")
data class PostRemoteKeys(
    @PrimaryKey
    val postId: Long,
    val prevKey: Long?,
    val nextKey: Long?
)
