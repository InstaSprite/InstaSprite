package com.instasprite.app.di

import com.instasprite.app.navigation.ResultEventBus
import com.instasprite.app.navigation.Screen
import com.instasprite.app.navigation.Navigator
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