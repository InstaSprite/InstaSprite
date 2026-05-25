package com.instasprite.app.ui.palette

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instasprite.app.navigation.EntryProviderInstaller
import com.instasprite.app.navigation.Navigator
import com.instasprite.app.navigation.Screen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object PaletteModule {

    @IntoSet
    @Provides
    fun providePaletteEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Screen.Palette> { args ->
            val viewModel = hiltViewModel<ColorPaletteViewModel>()

            ColorPaletteScreen(
                viewModel = viewModel,
                onDismiss = {
                    navigator.goBack()
                },
                onPaletteSelected = { palette ->
                    if (args.clickToReturn) {
                        navigator.goBackWithResult(palette)
                    } else {
                        navigator.goTo(Screen.PaletteEditor(palette))
                    }
                },
                onPaletteEdit = { palette ->
                    navigator.goTo(Screen.PaletteEditor(palette))
                },
                onCreateNewPalette = {
                    navigator.goTo(Screen.PaletteEditor(null))
                }
            )
        }
    }

    @IntoSet
    @Provides
    fun providePaletteEditorEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Screen.PaletteEditor> { args ->
            val viewModel = hiltViewModel<PaletteEditorViewModel, PaletteEditorViewModel.Factory> { factory ->
                factory.create(args.palette)
            }

            PaletteEditorScreen(
                viewModel = viewModel,
                onDismiss = {
                    navigator.goBack()
                }
            )
        }
    }
}