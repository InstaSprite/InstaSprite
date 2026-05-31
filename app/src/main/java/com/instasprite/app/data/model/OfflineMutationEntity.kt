package com.instasprite.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MutationType {
    LIKE_POST,
    UNLIKE_POST,
    CREATE_COMMENT,
    CREATE_POST
}

@Entity(tableName = "offline_mutations")
data class OfflineMutationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: MutationType,
    val payloadJson: String, // Action-specific details serialized as JSON
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)
