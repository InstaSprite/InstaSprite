package com.instasprite.app.ui.palette

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasprite.app.R
import com.instasprite.app.data.repository.ColorPaletteRepository
import com.instasprite.app.domain.dialog.DialogController
import com.instasprite.app.domain.model.ColorPalette
import com.instasprite.app.utils.getFileName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ColorPaletteViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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

    fun importPaletteFromGplUri(uri: Uri) {
        viewModelScope.launch {
            val (fileName, isValidGpl) = withContext(Dispatchers.IO) {
                val name = getFileName(context, uri)
                val isValid = name?.endsWith(".gpl", ignoreCase = true) == true
                name to isValid
            }

            if (!isValidGpl) {
                val onlyGplMessage = context.getString(R.string.only_gpl_supported)
                Toast.makeText(context, onlyGplMessage, Toast.LENGTH_SHORT).show()
                return@launch
            }

            val displayName = fileName?.removeSuffix(".gpl")?.removeSuffix(".GPL")
                ?: context.getString(R.string.importing)

            val result = withContext(Dispatchers.IO) {
                colorPaletteRepository.importPaletteFromGplUri(uri, displayName)
            }

            if (result != null) {
                val successMessage = context.getString(R.string.import_success, result.name)
                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
            } else {
                val errorMessage = context.getString(R.string.import_failed)
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}