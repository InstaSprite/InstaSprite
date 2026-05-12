package com.instasprite.app.domain.model

data class PageData (

    val content: List<PostData>,

    val nextCursor: Long?,

    val hasNext: Boolean
)