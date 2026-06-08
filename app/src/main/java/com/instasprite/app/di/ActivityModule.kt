package com.instasprite.app.di


import android.content.Context
import com.instasprite.app.navigation.Navigator
import com.instasprite.app.navigation.ResultEventBus
import com.instasprite.app.navigation.Screen
import com.instasprite.app.utils.AppSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object ActivityModule {
    @Provides
    @ActivityRetainedScoped
    fun provideNavigator(
        eventBus: ResultEventBus,
        @ApplicationContext context: Context
    ): Navigator {
        val hasSeenOnboarding = AppSettings.getHasSeenOnboarding(context)
        val startScreen = if (hasSeenOnboarding) Screen.Home else Screen.Onboarding
        return Navigator(startScreen, eventBus)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideResultEventBus(): ResultEventBus {
        return ResultEventBus()
    }
}