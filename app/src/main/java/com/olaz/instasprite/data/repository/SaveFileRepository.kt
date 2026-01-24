package com.olaz.instasprite.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.olaz.instasprite.data.mapper.toEntity
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.utils.getFormatFromExtension
import kotlinx.serialization.json.Json
import java.io.IOException

object SaveFileRepository {

    // Save as Image
    fun saveFile(
        context: Context,
        bitmap: Bitmap,
        folderUri: Uri,
        fileName: String,
    ): Boolean {
        val formatInfo = getFormatFromExtension(fileName) ?: return false
        val (compressFormat, mimeType) = formatInfo

        val folder = DocumentFile.fromTreeUri(context, folderUri)
        val file = folder?.createFile(mimeType, fileName) ?: return false
        val outputStream = context.contentResolver.openOutputStream(file.uri) ?: return false

        return try {
            bitmap.compress(compressFormat, 100, outputStream)
            outputStream.flush()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            outputStream.close()
        }
    }

    // Save as .isprite
    fun saveFile(
        context: Context,
        sprite: Sprite,
        folderUri: Uri,
        fileName: String
    ): Boolean {
        val finalFileName = if (fileName.endsWith(".isprite")) fileName else "$fileName.isprite"

        val folder = DocumentFile.fromTreeUri(context, folderUri)
        val file = folder?.createFile("application/isprite", finalFileName) ?: return false
        val outputStream = context.contentResolver.openOutputStream(file.uri) ?: return false

        return try {
            val spriteData = sprite.toEntity()
            val jsonData = Json.encodeToString(spriteData)
            outputStream.write(jsonData.toByteArray(Charsets.UTF_8))
            outputStream.flush()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            outputStream.close()
        }
    }
}