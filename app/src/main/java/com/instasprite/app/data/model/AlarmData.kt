package com.instasprite.app.data.model

import com.instasprite.app.domain.model.MemberData
import java.time.LocalDateTime

data class AlarmData(
    val id: Long,
    val type: String,
    val message: String,
    val agent: MemberData,
    val createdDate: LocalDateTime
)

enum class AlarmType {
    FOLLOW,
    LIKE,
    COMMENT,
    MENTION
}
