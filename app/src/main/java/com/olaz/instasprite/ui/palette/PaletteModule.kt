package com.olaz.instasprite.ui.palette

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.olaz.instasprite.navigation.EntryProviderInstaller
import com.olaz.instasprite.navigation.Navigator
import com.olaz.instasprite.navigation.Screen
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
                    navigator.goBack()
                }
            )
        }
    }
}