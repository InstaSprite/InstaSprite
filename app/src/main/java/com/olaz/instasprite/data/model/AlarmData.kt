package com.olaz.instasprite.data.model

import com.olaz.instasprite.domain.model.MemberData
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
