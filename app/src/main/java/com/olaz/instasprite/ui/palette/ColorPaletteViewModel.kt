package com.olaz.instasprite.ui.palette

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olaz.instasprite.data.repository.ColorPaletteRepository
import com.olaz.instasprite.domain.dialog.DialogController
import com.olaz.instasprite.domain.model.ColorPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColorPaletteViewModel @Inject constructor(
    private val colorPaletteRepository: ColorPaletteRepository,
    private val dialogController: DialogController<ColorPaletteDialog>
) : ViewModel(),
    DialogController<ColorPaletteDialog> by dialogController {

    val savedPalettes: StateFlow<List<ColorPalette>> = colorPaletteRepository.savedPalettes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    fun savePalette(palette: ColorPalette) {
        viewModelScope.launch {
            colorPaletteRepository.savePaletteToDB(palette)
        }
    }

    fun deletePalette(palette: ColorPalette) {
        viewModelScope.launch {
            colorPaletteRepository.deletePalette(palette.id)
        }
    }

    suspend fun importPaletteFromLospecUrl(url: String): ColorPalette? {
        return colorPaletteRepository.getLospecColorPalette(url)
    }
}