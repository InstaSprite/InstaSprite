package com.instasprite.app.domain.model

import android.content.Context
import com.instasprite.app.R
import java.time.LocalDateTime

data class GroupedNotificationData(
    val id: String,
    val groupKey: String,
    val type: NotificationType,
    val relatedEntityId: String?,
    val actorCount: Int,
    val isRead: Boolean,
    val updatedAt: LocalDateTime,
    val recentActors: List<ActorSummary>,
    val title: String?,
    val body: String?
) {
    data class ActorSummary(
        val name: String?,
        val username: String?,
        val avatarUrl: String?
    )

    fun buildDisplayText(context: Context): String {
        if (type == NotificationType.UNKNOWN && !title.isNullOrEmpty() && !body.isNullOrEmpty()) {
            return "$title $body"
        }

        val someone = context.getString(R.string.notification_someone)
        val firstActor = recentActors.firstOrNull()?.name ?: someone
        val secondActor = recentActors.getOrNull(1)?.name

        val actionText = context.getString(
            when (type) {
                NotificationType.LIKE -> R.string.notification_action_like
                NotificationType.COMMENT -> R.string.notification_action_comment
                NotificationType.FOLLOW -> R.string.notification_action_follow
                NotificationType.MENTION -> R.string.notification_action_mention
                NotificationType.UNKNOWN -> R.string.notification_action_unknown
            }
        )

        return when {
            actorCount == 1 -> context.getString(R.string.notification_format_single, firstActor, actionText)
            actorCount == 2 && secondActor != null -> context.getString(R.string.notification_format_double, firstActor, secondActor, actionText)
            actorCount > 2 -> {
                val othersCount = actorCount - 1
                context.resources.getQuantityString(
                    R.plurals.notification_format_multiple,
                    othersCount,
                    firstActor,
                    othersCount,
                    actionText
                )
            }
            else -> context.getString(R.string.notification_format_single, someone, actionText)
        }
    }
}
