package com.olaz.instasprite.domain.model

data class PageData (

    val content: List<PostData>,

    val nextCursor: Long?,

    val hasNext: Boolean
)