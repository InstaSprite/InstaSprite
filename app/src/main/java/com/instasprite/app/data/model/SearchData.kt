package com.instasprite.app.data.model

import com.instasprite.app.domain.model.MemberImageData
import java.time.LocalDateTime

data class SearchData(
    val dtype: String,
    val entityName: String,
    val entityType: String,
    val searchCount: Long = 0,
    val createdDate: LocalDateTime? = null
)

data class SearchMemberData(
    val memberId: Long,
    val memberUsername: String,
    val memberName: String,
    val memberImage: MemberImageData? = null,
    val isFollowing: Boolean = false
)

data class SearchHashtagData(
    val hashtagId: Long,
    val hashtagName: String,
    val postCount: Long = 0
)

data class RecommendMemberData(
    val memberId: Long,
    val memberUsername: String,
    val memberName: String,
    val memberImage: MemberImageData? = null,
    val isFollowing: Boolean = false
)
