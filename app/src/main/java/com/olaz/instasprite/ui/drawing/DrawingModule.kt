package com.olaz.instasprite.ui.drawing

import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.olaz.instasprite.navigation.EntryProviderInstaller
import com.olaz.instasprite.navigation.Navigator
import com.olaz.instasprite.navigation.Screen
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

            val scope = rememberCoroutineScope()

            DrawingScreen(
                viewModel = viewModel,
                onNavigateBack = { spriteId ->
                    scope.launch {
                        viewModel.saveToDB()
                        navigator.goBack()
                    }
                }
            )
        }
    }
}