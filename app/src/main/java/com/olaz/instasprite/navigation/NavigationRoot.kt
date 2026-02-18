package com.olaz.instasprite.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.olaz.instasprite.domain.model.ColorPalette
import com.olaz.instasprite.ui.drawing.DrawingScreen
import com.olaz.instasprite.ui.drawing.DrawingViewModel
import com.olaz.instasprite.ui.gallery.CreateCanvasScreen
import com.olaz.instasprite.ui.gallery.GalleryScreen
import com.olaz.instasprite.ui.gallery.GalleryViewModel
import com.olaz.instasprite.ui.palette.ColorPaletteScreen
import com.olaz.instasprite.ui.palette.ColorPaletteViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
sealed interface Screen : NavKey {

    @Serializable
    data object Gallery : Screen

    @Serializable
    data object Palette : Screen

    @Serializable
    data object CreateCanvas : Screen

    @Serializable
    data class Drawing(
        val spriteId: String,
        val width: Int,
        val height: Int,
        val spriteName: String?
    ) : Screen
}

internal const val TRANSITION_DURATION_MILLISECOND = 300

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(Screen.Gallery)

    var selectedPaletteResult by remember { mutableStateOf<ColorPalette?>(null) }
    var lastEditedSpriteIdResult by remember { mutableStateOf<String?>(null) }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = { screen ->
            when (screen) {
                Screen.Gallery -> NavEntry(screen) {
                    val viewModel = hiltViewModel<GalleryViewModel>()

                    LaunchedEffect(selectedPaletteResult, lastEditedSpriteIdResult) {
                        selectedPaletteResult?.let {
                            viewModel.onCanvasPaletteSelected(it)
                            selectedPaletteResult = null
                        }
                        lastEditedSpriteIdResult?.let {
                            viewModel.lastEditedSpriteId = it
                            lastEditedSpriteIdResult = null
                        }
                    }

                    GalleryScreen(
                        viewModel = viewModel,
                        onNavigateToDrawing = { id, width, height, name, _ ->
                            backStack.add(
                                Screen.Drawing(
                                    spriteId = id,
                                    width = width,
                                    height = height,
                                    spriteName = name
                                )
                            )
                        },
                        onNavigateToPalette = {
                            backStack.add(Screen.Palette)
                        },
                        onNavigateToCreateCanvas = {
                            backStack.add(Screen.CreateCanvas)
                        },
                    )
                }

                is Screen.Drawing -> NavEntry(screen) {
                    val viewModel =
                        hiltViewModel<DrawingViewModel, DrawingViewModel.Factory>
                        { factory ->
                            factory.create(
                                spriteId = screen.spriteId,
                                width = screen.width,
                                height = screen.height,
                                spriteName = screen.spriteName
                            )
                        }

                    val scope = rememberCoroutineScope()

                    DrawingScreen(
                        viewModel = viewModel,
                        onNavigateBack = { spriteId ->
                            scope.launch {
                                viewModel.saveToDB()
                                lastEditedSpriteIdResult = spriteId
                                backStack.removeLastOrNull()
                            }
                        }
                    )
                }

                Screen.Palette -> NavEntry(screen) {
                    val viewModel = hiltViewModel<ColorPaletteViewModel>()

                    ColorPaletteScreen(
                        viewModel = viewModel,
                        onDismiss = {
                            backStack.removeLastOrNull()
                        },
                        onPaletteSelected = { palette ->
                            selectedPaletteResult = palette
                            backStack.removeLastOrNull()
                        }
                    )
                }

                Screen.CreateCanvas -> NavEntry(screen) {
                    val selectedPalette = selectedPaletteResult
                    LaunchedEffect(selectedPalette) {
                        if (selectedPalette != null) {
                            selectedPaletteResult = null
                        }
                    }

                    CreateCanvasScreen(
                        onDismiss = { backStack.removeLastOrNull() },
                        onConfirm = { name, width, height ->
                            backStack.removeLastOrNull()

                            val id = UUID.randomUUID().toString()
                            backStack.add(
                                Screen.Drawing(
                                    spriteId = id,
                                    width = width,
                                    height = height,
                                    spriteName = name
                                )
                            )
                        },
                        onPaletteViewClick = {
                            backStack.add(Screen.Palette)
                        },
                        selectedPalette = selectedPalette
                    )
                }

                else -> error("Unknown screen: $screen")
            }
        }
    )
}