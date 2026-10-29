package com.instasprite.app.di

import android.content.Context
import com.instasprite.app.data.repository.ColorPaletteRepository
import com.instasprite.app.data.repository.PixelCanvasRepository
import com.instasprite.app.domain.canvashistory.HistoryDiskStore
import com.instasprite.app.domain.draw.DrawingEngine
import com.instasprite.app.domain.draw.DrawingSession
import com.instasprite.app.domain.usecase.PixelCanvasUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import java.io.File
import javax.inject.Inject

class DrawingSessionFactory @Inject constructor(
    private val pixelCanvasRepository: PixelCanvasRepository,
    private val colorPaletteRepository: ColorPaletteRepository,
    @ApplicationContext private val context: Context
) {
    fun create(spriteId: String, scope: CoroutineScope): DrawingSession {
        val useCase = PixelCanvasUseCase(pixelCanvasRepository, colorPaletteRepository)
        val historyStore = HistoryDiskStore(File(context.cacheDir, "history/$spriteId"))
        return DrawingSession(useCase, DrawingEngine(useCase, scope, historyStore), historyStore)
    }
}
