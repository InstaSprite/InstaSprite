package com.instasprite.app.di

import com.instasprite.app.data.network.sync.SyncManager
import com.instasprite.app.data.network.sync.SyncManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    @Binds
    abstract fun bindSyncManager(
        syncManagerImpl: SyncManagerImpl
    ): SyncManager
}
