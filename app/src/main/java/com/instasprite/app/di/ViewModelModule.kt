package com.instasprite.app.di

import android.content.Context
import com.instasprite.app.data.database.AppDatabase
import com.instasprite.app.data.network.lospec.LospecService
import com.instasprite.app.data.repository.ColorPaletteRepository
import com.instasprite.app.data.repository.FileRepository
import com.instasprite.app.data.repository.PixelCanvasRepository
import com.instasprite.app.data.repository.SortSettingRepository
import com.instasprite.app.domain.dialog.DialogController
import com.instasprite.app.domain.dialog.DialogControllerImpl
import com.instasprite.app.domain.model.PixelCanvas
import com.instasprite.app.ui.drawing.DrawingDialog
import com.instasprite.app.ui.gallery.GalleryDialog
import com.instasprite.app.ui.palette.ColorPaletteDialog
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
