package com.olaz.instasprite.data.network.model

data class UploadInitResponseDto(
    val postId: Long,
    val uploads: List<UploadTarget>
) {
    data class UploadTarget(
        val key: String,
        val url: String
    )
}
