package com.olaz.instasprite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.olaz.instasprite.domain.model.ColorPalette
import com.olaz.instasprite.navigation.CreateCanvasRoute
import com.olaz.instasprite.navigation.DrawingRoute
import com.olaz.instasprite.navigation.GalleryRoute
import com.olaz.instasprite.navigation.PaletteRoute
import com.olaz.instasprite.ui.drawing.DrawingScreen
import com.olaz.instasprite.ui.drawing.DrawingViewModel
import com.olaz.instasprite.ui.gallery.CreateCanvasScreen
import com.olaz.instasprite.ui.gallery.GalleryScreen
import com.olaz.instasprite.ui.gallery.GalleryViewModel
import com.olaz.instasprite.ui.palette.ColorPaletteScreen
import com.olaz.instasprite.ui.palette.ColorPaletteViewModel
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
                        
                        val selectedPalette = it.savedStateHandle.get<ColorPalette>("selected_palette")
                        val lastEditedSpriteId = it.savedStateHandle.get<String>("last_edited_sprite_id")
                        LaunchedEffect(selectedPalette, lastEditedSpriteId) {
                            if (selectedPalette != null) {
                                galleryViewModel.onCanvasPaletteSelected(selectedPalette)
                                it.savedStateHandle["selected_palette"] = null
                            }
                            if (lastEditedSpriteId != null) {
                                galleryViewModel.lastEditedSpriteId = lastEditedSpriteId
                                it.savedStateHandle["last_edited_sprite_id"] = null
                            }
                        }

                        GalleryScreen(
                            viewModel = galleryViewModel,
                            onNavigateToDrawing = { id, width, height, name, palette ->
                                navController.navigate(
                                    DrawingRoute(
                                        spriteId = id,
                                        width = width,
                                        height = height,
                                        spriteName = name
                                    )
                                )
                            },
                            onNavigateToPalette = {
                                navController.navigate(PaletteRoute)
                            },
                            onNavigateToCreateCanvas = {
                                navController.navigate(CreateCanvasRoute)
                            }
                        )
                    }

                    composable<CreateCanvasRoute> { backStackEntry ->
                        val selectedPalette = backStackEntry.savedStateHandle.get<ColorPalette>("selected_palette")
                        LaunchedEffect(selectedPalette) {
                            if (selectedPalette != null) {
                                backStackEntry.savedStateHandle["selected_palette"] = null
                            }
                        }

                        CreateCanvasScreen(
                            onDismiss = { navController.popBackStack() },
                            onConfirm = { name, width, height ->
                                navController.popBackStack()
                                
                                val id = java.util.UUID.randomUUID().toString()
                                navController.navigate(
                                    DrawingRoute(
                                        spriteId = id,
                                        width = width,
                                        height = height,
                                        spriteName = name
                                    )
                                )
                            },
                            onPaletteViewClick = {
                                navController.navigate(PaletteRoute)
                            },
                            selectedPalette = selectedPalette
                        )
                    }

                    composable<DrawingRoute> {
                        val drawingViewModel = hiltViewModel<DrawingViewModel>()
                        DrawingScreen(
                            viewModel = drawingViewModel,
                            onNavigateBack = { spriteId ->
                                lifecycleScope.launch {
                                    drawingViewModel.saveToDB()
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("last_edited_sprite_id", spriteId)
                                    navController.popBackStack()
                                }
                            }
                        )
                    }

                    composable<PaletteRoute> {
                        val paletteViewModel = hiltViewModel<ColorPaletteViewModel>()
                        ColorPaletteScreen(
                            viewModel = paletteViewModel,
                            onDismiss = {
                                navController.popBackStack()
                            },
                            onPaletteSelected = { palette ->
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("selected_palette", palette)
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}