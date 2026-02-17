package com.olaz.instasprite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.olaz.instasprite.ui.drawing.DrawingScreen
import com.olaz.instasprite.ui.drawing.DrawingViewModel
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DrawingActivity : ComponentActivity() {

    private val viewModel: DrawingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InstaSpriteTheme {
                DrawingScreen(
                    onNavigateBack = { finish() },
                    viewModel = viewModel
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            viewModel.saveToDB()
        }
    }

    companion object {
        const val EXTRA_CANVAS_WIDTH = "com.olaz.instasprite.CANVAS_WIDTH"
        const val EXTRA_CANVAS_HEIGHT = "com.olaz.instasprite.CANVAS_HEIGHT"
        const val EXTRA_SPRITE_ID = "com.olaz.instasprite.SPRITE_ID"
        const val EXTRA_SPRITE_NAME = "com.olaz.instasprite.SPRITE_NAME"
    }
}