package com.olaz.instasprite.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.olaz.instasprite.ISprite
import com.olaz.instasprite.data.mapper.toSprite
import com.olaz.instasprite.data.mapper.toISprite
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.utils.getFormatFromExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.PushbackInputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class FileRepository(val context: Context) {
    suspend fun saveISpriteFile(
        sprite: Sprite,
        folderUri: Uri,
        fileName: String
    ): Boolean = withContext(Dispatchers.IO) {
        val finalFileName = if (fileName.endsWith(".isprite")) fileName else "$fileName.isprite"

        val folder = DocumentFile.fromTreeUri(context, folderUri)
        val file = folder?.createFile("application/isprite", finalFileName) ?: return@withContext false

        val outputStream = context.contentResolver.openOutputStream(file.uri) ?: return@withContext false

        try {
            GZIPOutputStream(outputStream).use { gzipStream ->
                val proto = sprite.toISprite()
                proto.writeTo(gzipStream)
                gzipStream.finish()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun loadISpriteFile(fileUri: Uri): Sprite? {
        val inputStream = context.contentResolver.openInputStream(fileUri) ?: return null
        return try {
            PushbackInputStream(inputStream, 2).use { pushbackStream ->
                val header = ByteArray(2)
                val bytesRead = pushbackStream.read(header)

                if (bytesRead != -1) pushbackStream.unread(header, 0, bytesRead)

                // gzip magic number
                val isGzipped = bytesRead == 2 &&
                        (header[0].toInt() and 0xFF == 0x1F) &&
                        (header[1].toInt() and 0xFF == 0x8B)

                val finalStream = if (isGzipped) GZIPInputStream(pushbackStream) else pushbackStream

                val proto = ISprite.parseFrom(finalStream)

                proto.toSprite()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream.close()
        }
    }

    fun saveFile(
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
}
