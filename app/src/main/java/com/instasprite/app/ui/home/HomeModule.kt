package com.instasprite.app.ui.home

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instasprite.app.navigation.EntryProviderInstaller
import com.instasprite.app.navigation.Navigator
import com.instasprite.app.navigation.ResultEffect
import com.instasprite.app.navigation.Screen
import com.instasprite.app.ui.gallery.GalleryViewModel
import com.instasprite.app.ui.setting.SettingScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object HomeModule {

    @IntoSet
    @Provides
    fun provideHomeEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Screen.Home> {
            val galleryViewModel = hiltViewModel<GalleryViewModel>()

            ResultEffect<String>(navigator.eventBus) { spriteId ->
                galleryViewModel.lastEditedSpriteId = spriteId
            }

            HomeScreen(
                galleryViewModel = galleryViewModel,
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
                onNavigateToCreateCanvas = { navigator.goTo(Screen.CreateCanvas) },
                onNavigateToLoadImage = { navigator.goTo(Screen.LoadImage) },
                onOpenSetting = { navigator.goTo(Screen.Setting) },
                onOpenAbout = { navigator.goTo(Screen.About) },
                onNavigateToPalette = { navigator.goTo(Screen.Palette(clickToReturn = false)) },
            )
        }

        entry<Screen.Setting> {
            SettingScreen(
                onBackClick = { navigator.goBack() }
            )
        }

        entry<Screen.About> {
            AboutScreen(
                onBackClick = { navigator.goBack() }
            )
        }
    }
}
