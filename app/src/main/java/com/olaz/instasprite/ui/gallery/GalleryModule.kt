package com.olaz.instasprite.ui.gallery

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.olaz.instasprite.navigation.EntryProviderInstaller
import com.olaz.instasprite.navigation.Navigator
import com.olaz.instasprite.navigation.Screen
import com.olaz.instasprite.ui.palette.ColorPaletteScreen
import com.olaz.instasprite.ui.palette.ColorPaletteViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import java.util.UUID

@Module
@InstallIn(ActivityRetainedComponent::class)
object GalleryModule {

    @IntoSet
    @Provides
    fun provideGalleryEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Screen.Gallery> { args ->
            val viewModel = hiltViewModel<GalleryViewModel>()
            GalleryScreen(
                viewModel = viewModel,
                onNavigateToDrawing = { id, width, height, name, _ ->
                    navigator.goTo(
                        Screen.Drawing(
                            spriteId = id,
                            width = width,
                            height = height,
                            spriteName = name
                        )
                    )
                },
                onNavigateToPalette = {
                    navigator.goTo(Screen.Palette)
                },
                onNavigateToCreateCanvas = {
                    navigator.goTo(Screen.CreateCanvas)
                },
            )
        }

        entry<Screen.CreateCanvas> { args ->
            CreateCanvasScreen(
                onDismiss = { navigator.goBack() },
                onConfirm = { name, width, height ->
                    navigator.goBack()

                    val id = UUID.randomUUID().toString()
                    navigator.goTo(
                        Screen.Drawing(
                            spriteId = id,
                            width = width,
                            height = height,
                            spriteName = name
                        )
                    )
                },
                onPaletteViewClick = {
                    navigator.goTo(Screen.Palette)
                },
                selectedPalette = null,
            )
        }
    }
}