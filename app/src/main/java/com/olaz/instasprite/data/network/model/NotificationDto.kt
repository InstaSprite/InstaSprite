package com.olaz.instasprite.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: String,
    val title: String,
    val body: String,
    val type: String, // LIKE, FOLLOW, MENTION, COMMENT
    val relatedEntityId: String? = null,
    val isRead: Boolean,
    val createdAt: String,
    val senderName: String? = null,
    val senderUsername: String? = null,
    val senderAvatarUrl: String? = null
)
