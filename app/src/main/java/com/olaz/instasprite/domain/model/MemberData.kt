package com.olaz.instasprite.domain.model

data class MemberData(
    val memberId: Long,
    val memberUsername: String,
    val memberName: String,
    val memberImage: MemberImageData? = null
)

