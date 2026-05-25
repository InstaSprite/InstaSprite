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
import com.instasprite.app.utils.AppSettings
import com.instasprite.app.utils.getFileName
import com.instasprite.app.di.settingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showSearchBar = MutableStateFlow(false)
    val showSearchBar: StateFlow<Boolean> = _showSearchBar.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearchBar() {
        _showSearchBar.value = !_showSearchBar.value
        if (!_showSearchBar.value) {
            _searchQuery.value = ""
        }
    }

    val savedPalettes: StateFlow<List<ColorPalette>> = combine(
        colorPaletteRepository.savedPalettes,
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.author.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoritePalettes: StateFlow<List<ColorPalette>> = combine(
        colorPaletteRepository.savedPalettes,
        _searchQuery
    ) { list, query ->
        val favorites = list.filter { it.isFavorite }
        if (query.isBlank()) {
            favorites
        } else {
            favorites.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.author.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val defaultPaletteId: StateFlow<Int> = context.settingsDataStore.data
        .map { it.defaultPaletteId }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = AppSettings.getDefaultPaletteId(context)
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

    fun toggleFavorite(palette: ColorPalette) {
        viewModelScope.launch {
            colorPaletteRepository.setFavorite(palette.id, !palette.isFavorite)
        }
    }

    fun setDefaultPalette(paletteId: Int) {
        AppSettings.setDefaultPaletteId(context, paletteId)
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