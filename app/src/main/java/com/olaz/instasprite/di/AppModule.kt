package com.olaz.instasprite.di

import android.content.Context
import com.olaz.instasprite.data.database.AppDatabase
import com.olaz.instasprite.domain.model.PixelCanvas
import com.olaz.instasprite.data.network.lospec.LospecService
import com.olaz.instasprite.data.repository.*
import com.olaz.instasprite.domain.dialog.DialogController
import com.olaz.instasprite.domain.dialog.DialogControllerImpl
import com.olaz.instasprite.ui.drawing.DrawingDialog
import com.olaz.instasprite.ui.gallery.GalleryDialog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideSpriteRepository(db: AppDatabase): SpriteDatabaseRepository {
        return SpriteDatabaseRepository(
            dao = db.spriteDataDao(),
            metaDao = db.spriteMetaDataDao()
        )
    }

    @Provides
    @Singleton
    fun provideStorageLocationRepository(@ApplicationContext context: Context): StorageLocationRepository {
        return StorageLocationRepository(context)
    }

    @Provides
    fun provideColorPaletteRepository(
        @ApplicationContext context: Context, db: AppDatabase, lospecService: LospecService
    ): ColorPaletteRepository {
        return ColorPaletteRepository(context, db.colorPaletteDao(), lospecService)
    }

    @Provides
    fun providePixelCanvasRepository(): PixelCanvasRepository {
        // Default 16x16, viewModel will resize this based on the Intent extras.
        return PixelCanvasRepository(PixelCanvas(16, 16))
    }


    @Provides
    fun provideSortSettingRepository(@ApplicationContext context: Context): SortSettingRepository {
        return SortSettingRepository(context)
    }

    @Provides
    fun provideDrawingDialogController(): DialogController<DrawingDialog> {
        return DialogControllerImpl()
    }

    @Provides
    fun provideGalleryDialogController(): DialogController<GalleryDialog> {
        return DialogControllerImpl()
    }
}
