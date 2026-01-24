package com.olaz.instasprite.domain.usecase

import android.content.Context
import android.net.Uri

import com.olaz.instasprite.data.repository.LoadFileRepository

class LoadFileUseCase {
    fun loadFile(context: Context, fileUri: Uri): com.olaz.instasprite.domain.model.Sprite? {
        return LoadFileRepository.loadFile(context, fileUri)
    }
}
