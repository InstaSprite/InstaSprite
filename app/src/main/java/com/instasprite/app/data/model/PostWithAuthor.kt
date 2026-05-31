package com.instasprite.app.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class PostWithAuthor(
    @Embedded val post: PostEntity,
    @Relation(
        parentColumn = "authorId",
        entityColumn = "memberId"
    )
    val author: UserEntity?
)
