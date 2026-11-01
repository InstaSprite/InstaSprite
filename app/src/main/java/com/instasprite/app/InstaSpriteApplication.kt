package com.instasprite.app

import android.app.Application
import android.util.Log
import com.instasprite.app.data.diagnostics.CrashReports
import com.instasprite.app.di.settingsDataStore
import kotlinx.coroutines.flow.first
import com.instasprite.app.data.repository.SpriteDatabaseRepository
import javax.inject.Inject
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.instasprite.app.domain.canvashistory.HistoryDiskStore
import java.io.IOException

@HiltAndroidApp
class InstaSpriteApplication : Application() {
    @Inject lateinit var spriteRepository: SpriteDatabaseRepository

    override fun onCreate() {
        super.onCreate()
        val abandoned = try {
            HistoryDiskStore.retireAbandoned(cacheDir)
        } catch (e: IOException) {
            Log.w("InstaSprite", "Could not retire abandoned undo history", e)
            emptyList()
        }
        CoroutineScope(Dispatchers.IO).launch {
            CrashReports.setEnabled(settingsDataStore.data.first().crashReportsEnabled)
            try {
                spriteRepository.cleanupOrphanedSpriteFiles()
            } catch (e: Exception) {
                Log.w("InstaSprite", "Could not check orphaned sprite files", e)
            }
            abandoned.forEach {
                if (!it.deleteRecursively()) Log.w("InstaSprite", "Could not delete abandoned undo history")
            }
        }
    }
}
