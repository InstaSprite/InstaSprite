package com.instasprite.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val memberId: Long,
    val username: String,
    val name: String,
    val avatarUrl: String?
)
