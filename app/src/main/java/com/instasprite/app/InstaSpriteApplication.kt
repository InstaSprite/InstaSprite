package com.instasprite.app

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.instasprite.app.domain.canvashistory.HistoryDiskStore
import java.io.IOException

@HiltAndroidApp
class InstaSpriteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val abandoned = try {
            HistoryDiskStore.retireAbandoned(cacheDir)
        } catch (e: IOException) {
            Log.w("InstaSprite", "Could not retire abandoned undo history", e)
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            abandoned.forEach {
                if (!it.deleteRecursively()) Log.w("InstaSprite", "Could not delete abandoned undo history")
            }
        }
    }
}
