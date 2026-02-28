package com.olaz.instasprite.domain.usecase

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.olaz.instasprite.data.repository.SaveFileRepository
import com.olaz.instasprite.domain.export.ImageExporter
import com.olaz.instasprite.domain.model.Sprite

class SaveFileUseCase {
    fun saveImageFile(
        context: Context,
        sprite: Sprite,
        scalePercent: Int = 100,
        folderUri: Uri,
        fileName: String
    ): Result<Unit> {
        if (fileName.isBlank()) {
            return Result.failure(IllegalArgumentException("File name cannot be blank"))
        }

        val bitmap =
            ImageExporter.convertToBitmap(
                sprite.compositedPixels,
                sprite.width,
                sprite.height,
                scalePercent
            )
        if (bitmap == null) {
            return Result.failure(IllegalArgumentException("Failed to convert image"))
        }

        val success = SaveFileRepository.saveFile(context, bitmap, folderUri, fileName)
        return if (success) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to save file"))
        }
    }

    fun saveISpriteFile(
        context: Context,
        sprite: Sprite,
        folderUri: Uri,
        fileName: String
    ): Result<Unit> {
        if (fileName.isBlank()) {
            return Result.failure(IllegalArgumentException("File name cannot be blank"))
        }

        val success = SaveFileRepository.saveFile(context, sprite, folderUri, fileName)
        return if (success) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to save file"))
        }
    }
}