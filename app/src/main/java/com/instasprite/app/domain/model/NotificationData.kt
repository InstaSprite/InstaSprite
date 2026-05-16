package com.instasprite.app.domain.model

import java.time.LocalDateTime

data class NotificationData(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val relatedEntityId: String?,
    val isRead: Boolean,
    val createdAt: LocalDateTime,
    val senderName: String?,
    val senderUsername: String?,
    val senderAvatarUrl: String?
)

enum class NotificationType {
    LIKE,
    FOLLOW,
    MENTION,
    COMMENT,
    UNKNOWN;

    companion object {
        fun fromString(type: String?): NotificationType {
            return entries.find { it.name.equals(type, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
