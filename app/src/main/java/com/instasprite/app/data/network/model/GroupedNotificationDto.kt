package com.instasprite.app.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class GroupedNotificationDto(
    val id: String,
    val groupKey: String,
    val type: String,
    val relatedEntityId: String?,
    val actorCount: Int,
    val isRead: Boolean,
    val updatedAt: String,
    val recentActors: List<ActorSummaryDto>,
    val title: String?,
    val body: String?
)

@Serializable
data class ActorSummaryDto(
    val name: String?,
    val username: String?,
    val avatarUrl: String?
)
