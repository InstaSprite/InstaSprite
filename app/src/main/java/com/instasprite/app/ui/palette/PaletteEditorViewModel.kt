package com.instasprite.app.ui.palette

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasprite.app.data.repository.ColorPaletteRepository
import com.instasprite.app.domain.model.ColorPalette
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = PaletteEditorViewModel.Factory::class)
class PaletteEditorViewModel @AssistedInject constructor(
    @Assisted("initialPalette") val initialPalette: ColorPalette?,
    private val colorPaletteRepository: ColorPaletteRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("initialPalette") initialPalette: ColorPalette?): PaletteEditorViewModel
    }

    private val _paletteName = MutableStateFlow(initialPalette?.name ?: "New Palette")
    val paletteName: StateFlow<String> = _paletteName.asStateFlow()

    private val _paletteAuthor = MutableStateFlow(initialPalette?.author ?: "Anonymous")
    val paletteAuthor: StateFlow<String> = _paletteAuthor.asStateFlow()

    private val _paletteColors = MutableStateFlow<List<Color>>(initialPalette?.colors?.toList() ?: emptyList())
    val paletteColors: StateFlow<List<Color>> = _paletteColors.asStateFlow()

    private val _editingColorIndex = MutableStateFlow<Int?>(null) // null = closed, -1 = adding new color, >= 0 = index of color being edited
    val editingColorIndex: StateFlow<Int?> = _editingColorIndex.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun updateName(name: String) {
        _paletteName.value = name
    }

    fun updateAuthor(author: String) {
        _paletteAuthor.value = author
    }

    fun openAddColorDialog() {
        _editingColorIndex.value = -1
    }

    fun openEditColorDialog(index: Int) {
        _editingColorIndex.value = index
    }

    fun closeColorDialog() {
        _editingColorIndex.value = null
    }

    fun saveColor(color: Color) {
        val index = _editingColorIndex.value ?: return
        val currentColors = _paletteColors.value.toMutableList()
        if (index == -1) {
            currentColors.add(color)
        } else if (index in currentColors.indices) {
            currentColors[index] = color
        }
        _paletteColors.value = currentColors
        closeColorDialog()
    }

    fun deleteColor(index: Int) {
        val currentColors = _paletteColors.value.toMutableList()
        if (index in currentColors.indices) {
            currentColors.removeAt(index)
            _paletteColors.value = currentColors
        }
    }

    fun reorderColors(fromIndex: Int, toIndex: Int) {
        val currentColors = _paletteColors.value.toMutableList()
        if (fromIndex in currentColors.indices && toIndex in currentColors.indices) {
            currentColors.add(toIndex, currentColors.removeAt(fromIndex))
            _paletteColors.value = currentColors
        }
    }

    fun updateColors(colors: List<Color>) {
        _paletteColors.value = colors
    }

    fun savePalette() {
        if (_isSaving.value) return
        _isSaving.value = true
        viewModelScope.launch {
            try {
                val palette = ColorPalette(
                    id = initialPalette?.id ?: 0,
                    name = _paletteName.value.ifBlank { "New Palette" },
                    author = _paletteAuthor.value.ifBlank { "Anonymous" },
                    colors = _paletteColors.value.toMutableList()
                )
                // Save custom palette to db (forceSave = true so it replaces the existing ID or inserts new one)
                colorPaletteRepository.savePaletteToDB(palette, forceSave = true)
                _saveSuccess.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }
}
