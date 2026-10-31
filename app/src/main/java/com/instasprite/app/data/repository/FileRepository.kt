package com.instasprite.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.google.protobuf.CodedInputStream
import com.instasprite.app.ISprite
import com.instasprite.app.utils.SpriteReadBudget
import com.instasprite.app.data.mapper.toISprite
import com.instasprite.app.data.mapper.toSprite
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.utils.getFormatFromExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
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
        val exportContext = kotlinx.coroutines.currentCoroutineContext()
        val name = fileName.removeSuffix(".isprite") + ".isprite"
        writeExportFile(folderUri, name, "application/isprite") { output ->
            exportContext.ensureActive()
            GZIPOutputStream(output).use { gzip ->
                val proto = sprite.toISprite()
                exportContext.ensureActive()
                proto.writeTo(gzip)
                exportContext.ensureActive()
            }
        }
    }

    suspend fun saveAnimationFile(
        sprite: Sprite, format: com.instasprite.app.domain.export.AnimationExportFormat,
        folderUri: Uri, fileName: String, columns: Int = 1, scalePercent: Int = 100
    ): Boolean = withContext(Dispatchers.IO) {
        val exportContext = kotlinx.coroutines.currentCoroutineContext()
        val name = fileName.removeSuffix(".${format.extension}") + ".${format.extension}"
        writeExportFile(folderUri, name, format.mimeType) { output ->
            when (format) {
                com.instasprite.app.domain.export.AnimationExportFormat.GIF ->
                    com.instasprite.app.domain.export.GifExporter.write(
                        sprite,
                        output,
                        scalePercent
                    ) { exportContext.ensureActive() }

                com.instasprite.app.domain.export.AnimationExportFormat.SPRITE_SHEET -> {
                    val bitmap =
                        com.instasprite.app.domain.export.ImageExporter.convertToSpriteSheet(
                            sprite,
                            columns,
                            scalePercent
                        ) {
                            exportContext.ensureActive()
                        }
                    try {
                        exportContext.ensureActive()
                        if (!bitmap.compress(
                                Bitmap.CompressFormat.PNG,
                                100,
                                output
                            )
                        ) throw IOException("PNG export failed")
                    } finally {
                        bitmap.recycle()
                    }
                }
            }
        }
    }

    private fun writeExportFile(
        folderUri: Uri, fileName: String, mimeType: String,
        write: (java.io.OutputStream) -> Unit
    ): Boolean {
        var file: DocumentFile? = null
        var complete = false
        try {
            val folder = DocumentFile.fromTreeUri(context, folderUri) ?: return false
            val stem = fileName.substringBeforeLast(".", fileName)
            val extension = fileName.substringAfterLast(".", "")
            var uniqueName = fileName
            var counter = 1
            while (folder.findFile(uniqueName) != null) uniqueName = "$stem (${counter++}).$extension"
            file = folder.createFile(mimeType, uniqueName) ?: return false
            val stream = context.contentResolver.openOutputStream(file.uri) ?: return false
            stream.use { write(it) }
            complete = true
            return true
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            return false
        } finally {
            if (!complete) runCatching { file?.delete() }
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

                val budget = SpriteReadBudget()
                val codedInput = CodedInputStream.newInstance(budget.limit(finalStream))
                codedInput.setSizeLimit(64 * 1024 * 1024)
                val proto = ISprite.parseFrom(codedInput)

                proto.toSprite(budget)
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
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

        return writeExportFile(folderUri, fileName, mimeType) { output ->
            if (!bitmap.compress(compressFormat, 100, output)) throw IOException("Image export failed")
        }
    }
}
