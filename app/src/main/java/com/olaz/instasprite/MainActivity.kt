package com.olaz.instasprite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.olaz.instasprite.navigation.DrawingRoute
import com.olaz.instasprite.navigation.GalleryRoute
import com.olaz.instasprite.ui.drawing.DrawingScreen
import com.olaz.instasprite.ui.drawing.DrawingViewModel
import com.olaz.instasprite.ui.gallery.GalleryScreen
import com.olaz.instasprite.ui.gallery.GalleryViewModel
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InstaSpriteTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = GalleryRoute
                ) {
                    composable<GalleryRoute> {
                        val galleryViewModel = hiltViewModel<GalleryViewModel>()
                        GalleryScreen(
                            viewModel = galleryViewModel,
                            onNavigateToDrawing = { id, width, height, name ->
                                navController.navigate(
                                    DrawingRoute(
                                        spriteId = id,
                                        width = width,
                                        height = height,
                                        spriteName = name
                                    )
                                )
                            }
                        )
                    }

                    composable<DrawingRoute> {
                        val drawingViewModel = hiltViewModel<DrawingViewModel>()
                        DrawingScreen(
                            viewModel = drawingViewModel,
                            onNavigateBack = {
                                lifecycleScope.launch {
                                    drawingViewModel.saveToDB()
                                    navController.popBackStack()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}