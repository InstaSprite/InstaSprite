package com.instasprite.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instasprite.app.data.network.model.ActorSummaryDto

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val groupKey: String,
    val type: String,
    val relatedEntityId: String?,
    val actorCount: Int,
    val isRead: Boolean,
    val updatedAt: String?,
    val recentActors: List<ActorSummaryDto>,
    val title: String?,
    val body: String?
)
