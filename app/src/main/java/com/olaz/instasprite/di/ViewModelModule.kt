package com.olaz.instasprite.di

import android.content.Context
import com.olaz.instasprite.data.database.AppDatabase
import com.olaz.instasprite.data.network.lospec.LospecService
import com.olaz.instasprite.data.repository.ColorPaletteRepository
import com.olaz.instasprite.data.repository.FileRepository
import com.olaz.instasprite.data.repository.PixelCanvasRepository
import com.olaz.instasprite.data.repository.SortSettingRepository
import com.olaz.instasprite.domain.dialog.DialogController
import com.olaz.instasprite.domain.dialog.DialogControllerImpl
import com.olaz.instasprite.domain.model.PixelCanvas
import com.olaz.instasprite.ui.drawing.DrawingDialog
import com.olaz.instasprite.ui.gallery.GalleryDialog
import com.olaz.instasprite.ui.palette.ColorPaletteDialog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext


@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

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
    fun provideFileRepository(@ApplicationContext context: Context): FileRepository {
        return FileRepository(context)
    }

    @Provides
    fun provideDrawingDialogController(): DialogController<DrawingDialog> {
        return DialogControllerImpl()
    }

    @Provides
    fun provideGalleryDialogController(): DialogController<GalleryDialog> {
        return DialogControllerImpl()
    }

    @Provides
    fun provideColorPaletteDialogController(): DialogController<ColorPaletteDialog> {
        return DialogControllerImpl()
    }
}
