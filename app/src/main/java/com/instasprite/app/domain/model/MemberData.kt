package com.instasprite.app.domain.model

data class MemberData(
    val memberId: Long,
    val memberUsername: String,
    val memberName: String,
    val memberImage: MemberImageData? = null
)

