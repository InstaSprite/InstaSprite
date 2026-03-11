package com.olaz.instasprite.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.olaz.instasprite.SpritePixels
import com.olaz.instasprite.data.mapper.toSpritePixels
import com.olaz.instasprite.domain.model.Layer
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.utils.getFormatFromExtension
import java.io.IOException

class FileRepository(val context: Context) {
    fun saveISpriteFile(
        sprite: Sprite,
        folderUri: Uri,
        fileName: String
    ): Boolean {
        val finalFileName = if (fileName.endsWith(".isprite")) fileName else "$fileName.isprite"

        val folder = DocumentFile.fromTreeUri(context, folderUri)
        val file = folder?.createFile("application/isprite", finalFileName) ?: return false
        val outputStream = context.contentResolver.openOutputStream(file.uri) ?: return false

        return try {
            val proto = sprite.toSpritePixels()
            proto.writeTo(outputStream)
            outputStream.flush()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            outputStream.close()
        }
    }

    fun loadISpriteFile(fileUri: Uri): Sprite? {
        val inputStream = context.contentResolver.openInputStream(fileUri) ?: return null
        return try {
            val proto = SpritePixels.parseFrom(inputStream)
            val layers = proto.layersList.map { layerData ->
                Layer(
                    id = layerData.id,
                    name = layerData.name,
                    isVisible = layerData.isVisible,
                    isLocked = layerData.isLocked,
                    pixels = layerData.pixelsList.toIntArray()
                )
            }
            Sprite(
                id = "", // Generates empty ID; app creates unique DB ID on save
                width = proto.width,
                height = proto.height,
                layers = layers,
                colorPalette = proto.colorPaletteList.takeIf { it.isNotEmpty() }
            )
        } catch (e: IOException) {
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
