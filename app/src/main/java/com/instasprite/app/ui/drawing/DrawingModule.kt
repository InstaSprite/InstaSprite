package com.instasprite.app.ui.drawing

import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import kotlinx.coroutines.launch

@Module
@InstallIn(ActivityRetainedComponent::class)
object DrawingModule {

    @IntoSet
    @Provides
    fun provideDrawingEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Screen.Drawing> { args ->
            val viewModel =
                hiltViewModel<DrawingViewModel, DrawingViewModel.Factory>
                { factory ->
                    factory.create(
                        spriteId = args.spriteId,
                        width = args.width,
                        height = args.height,
                        spriteName = args.spriteName
                    )
                }

            args.colorPalette?.let { viewModel.updateColorPalette(it.colors) }

            ResultEffect<ColorPalette>(navigator.eventBus) {
                viewModel.updateColorPalette(it.colors)
            }

            val scope = rememberCoroutineScope()

            DrawingScreen(
                viewModel = viewModel,
                onNavigateToPalette = {
                    navigator.goTo(Screen.Palette)
                },
                onNavigateBack = { spriteId ->
                    scope.launch {
                        viewModel.saveToDB()
                        navigator.goBackWithResult(spriteId)
                    }
                }
            )
        }
    }
}