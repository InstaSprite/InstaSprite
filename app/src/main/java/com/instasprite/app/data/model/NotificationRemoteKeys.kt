package com.instasprite.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_remote_keys")
data class NotificationRemoteKeys(
    @PrimaryKey
    val id: String,
    val prevKey: Int?,
    val nextKey: Int?
)
