package com.olaz.instasprite.data.network.model

data class UploadInitRequestDto(
    val files: List<FileInfo>
) {
    data class FileInfo(
        val contentType: String,
        val width: Int = 1080,
        val height: Int = 1080
    )
}
