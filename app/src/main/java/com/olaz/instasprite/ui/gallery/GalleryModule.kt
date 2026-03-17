package com.olaz.instasprite.ui.gallery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.olaz.instasprite.domain.model.ColorPalette
import com.olaz.instasprite.navigation.EntryProviderInstaller
import com.olaz.instasprite.navigation.Navigator
import com.olaz.instasprite.navigation.ResultEffect
import com.olaz.instasprite.navigation.Screen
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
                onConfirm = { name, width, height ->
                    val id = UUID.randomUUID().toString()

                    navigator.replace(
                        Screen.Drawing(
                            spriteId = id,
                            width = width,
                            height = height,
                            spriteName = name,
                            colorPalette = selectedPalette
                        )
                    )
                },
                onPaletteViewClick = {
                    navigator.goTo(Screen.Palette)
                },
                selectedPalette = selectedPalette,
            )
        }
    }
}