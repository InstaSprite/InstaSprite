package com.instasprite.app.di

import android.content.Context
import com.instasprite.app.data.database.AppDatabase
import com.instasprite.app.data.repository.*
import com.instasprite.app.data.source.ISpriteDatSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideSpriteRepository(db: AppDatabase, @ApplicationContext context: Context): SpriteDatabaseRepository {
        return SpriteDatabaseRepository(
            dao = db.spriteDataDao(),
            metaDao = db.spriteMetaDataDao(),
            pixelDataSource = ISpriteDatSource(context),
            context = context
        )
    }

    @Provides
    @Singleton
    fun provideStorageLocationRepository(@ApplicationContext context: Context): StorageLocationRepository {
        return StorageLocationRepository(context)
    }
}
