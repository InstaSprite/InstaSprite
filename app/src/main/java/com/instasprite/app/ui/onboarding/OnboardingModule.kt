package com.instasprite.app.ui.onboarding

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
object OnboardingModule {

    @Provides
    @IntoSet
    fun provideOnboardingEntry(navigator: Navigator): EntryProviderInstaller = {
        entry<Screen.Onboarding> {
            OnboardingScreen(
                onNavigateHome = {
                    navigator.replace(Screen.Home)
                }
            )
        }
    }
}
