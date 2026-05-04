package com.olaz.instasprite.domain.model

import java.time.LocalDateTime

data class CommentData(
    val id: Long,
    val member: MemberData,
    val content: String,
    val uploadDate: LocalDateTime,
    val commentLikesCount: Int = 0,
    val commentLikeFlag: Boolean = false,
    val repliesCount: Int = 0,
    val mentionsOfContent: List<String> = emptyList(),
    val hashtagsOfContent: List<String> = emptyList()
)

