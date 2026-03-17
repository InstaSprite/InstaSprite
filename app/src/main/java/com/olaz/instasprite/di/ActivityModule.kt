package com.olaz.instasprite.di

import com.olaz.instasprite.navigation.ResultEventBus
import com.olaz.instasprite.navigation.Screen
import com.olaz.instasprite.navigation.Navigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped


@Module
@InstallIn(ActivityRetainedComponent::class)
object ActivityModule {
    @Provides
    @ActivityRetainedScoped
    fun provideNavigator(eventBus: ResultEventBus): Navigator {
        return Navigator(Screen.Home, eventBus)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideResultEventBus(): ResultEventBus {
        return ResultEventBus()
    }
}