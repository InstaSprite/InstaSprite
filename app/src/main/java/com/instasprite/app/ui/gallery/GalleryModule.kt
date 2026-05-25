package com.instasprite.app.ui.gallery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.instasprite.app.domain.model.ColorPalette
import com.instasprite.app.navigation.EntryProviderInstaller
import com.instasprite.app.navigation.Navigator
import com.instasprite.app.navigation.ResultEffect
import com.instasprite.app.navigation.Screen
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

        entry<Screen.CreateCanvas> { args ->

            var selectedPalette by remember { mutableStateOf<ColorPalette?>(null) }

            ResultEffect<ColorPalette>(navigator.eventBus) { palette ->
                selectedPalette = palette
            }

            CreateCanvasScreen(
                onDismiss = { navigator.goBack() },
                onConfirm = { name, width, height, palette ->
                    val id = UUID.randomUUID().toString()

                    navigator.replace(
                        Screen.Drawing(
                            spriteId = id,
                            width = width,
                            height = height,
                            spriteName = name,
                            colorPalette = palette
                        )
                    )
                },
                onPaletteViewClick = {
                    navigator.goTo(Screen.Palette(clickToReturn = true))
                },
                selectedPalette = selectedPalette,
            )
        }
    }
}