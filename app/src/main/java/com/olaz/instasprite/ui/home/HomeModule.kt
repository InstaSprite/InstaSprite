package com.olaz.instasprite.ui.home

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.olaz.instasprite.navigation.EntryProviderInstaller
import com.olaz.instasprite.navigation.Navigator
import com.olaz.instasprite.navigation.ResultEffect
import com.olaz.instasprite.navigation.Screen
import com.olaz.instasprite.ui.gallery.GalleryViewModel
import com.olaz.instasprite.ui.setting.SettingScreen
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
                onLoginClick = { navigator.goTo(Screen.Auth) },
                onOpenComments = { postId -> navigator.goTo(Screen.Comments(postId)) },
                onOpenProfile = { userId -> navigator.goTo(Screen.Profile(userId)) },
                onOpenNotifications = { navigator.goTo(Screen.Notification) },
                onOpenSearch = { navigator.goTo(Screen.Search) },
                onOpenSetting = { navigator.goTo(Screen.Setting) },
                onNavigateToCreatePost = { navigator.goTo(Screen.CreatePost) },
            )
        }

        entry<Screen.Setting> {
            SettingScreen(
                onBackClick = { navigator.goBack() }
            )
        }
    }
}

