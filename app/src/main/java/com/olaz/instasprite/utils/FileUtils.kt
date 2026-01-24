package com.olaz.instasprite.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns


fun getFormatFromExtension(fileName: String): Pair<Bitmap.CompressFormat, String>? {
    return when (fileName.substringAfterLast('.', "").lowercase()) {
        "png" -> Bitmap.CompressFormat.PNG to "image/png"
        "jpg", "jpeg" -> Bitmap.CompressFormat.JPEG to "image/jpeg"
        "webp" -> Bitmap.CompressFormat.WEBP to "image/webp"
        else -> null
    }
}

fun getFullPathFromTreeUri(treeUri: Uri): String {
    return try {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val parts = docId.split(":")

        if (parts.size < 2) return "Selected folder"

        val storageType = parts[0]
        val pathPart = parts[1]

        when (storageType) {
            "primary" -> {
                if (pathPart.isEmpty() || pathPart == "/") {
                    "Internal Storage"
                } else {
                    "Internal Storage/$pathPart"
                }
            }

            else -> {
                if (pathPart.isEmpty() || pathPart == "/") {
                    "External Storage ($storageType)"
                } else {
                    "External Storage ($storageType)/$pathPart"
                }
            }
        }
    } catch (_: Exception) {
        "Selected folder"
    }
}

fun getFileName(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) return it.getString(nameIndex)
        }
    }
    return null
}
