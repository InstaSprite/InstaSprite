package com.instasprite.app.ui.loadimage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasprite.app.data.repository.SpriteDatabaseRepository
import com.instasprite.app.domain.image2pixel.PixelArtConfig
import com.instasprite.app.domain.image2pixel.PixelArtConverter
import com.instasprite.app.domain.model.Cel
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.Sprite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.UUID
import javax.inject.Inject
import com.instasprite.app.ui.loadimage.contract.ImageConfigEvent
import com.instasprite.app.domain.model.ColorPalette
import com.instasprite.app.ui.loadimage.contract.LoadImageUiState

@HiltViewModel
class LoadImageViewModel @Inject constructor(
    private val spriteDatabaseRepository: SpriteDatabaseRepository
) : ViewModel() {

    private val converter = PixelArtConverter()

    private val _uiState = MutableStateFlow(LoadImageUiState())
    val uiState: StateFlow<LoadImageUiState> = _uiState.asStateFlow()

    fun setPaletteAndApplyConf(selectedPalette: ColorPalette?, customPaletteColors: List<Int>?) {
        _uiState.update { it.copy(selectedPalette = selectedPalette) }
        updateConfig { c -> c.copy(customPalette = customPaletteColors) }
    }

    fun onEvent(event: ImageConfigEvent) {
        when (event) {
            is ImageConfigEvent.SpriteNameChange -> _uiState.update { it.copy(spriteName = event.name) }
            is ImageConfigEvent.ApplyPaletteChange -> _uiState.update { it.copy(applyPalette = event.apply) }
            is ImageConfigEvent.TargetWidthChange -> updateConfig { it.copy(targetWidth = event.width, autoDetect = false) }
            is ImageConfigEvent.ColorCountChange -> updateConfig { it.copy(colorCount = event.count) }
            is ImageConfigEvent.AutoDetectChange -> updateConfig { it.copy(autoDetect = event.autoDetect) }
            is ImageConfigEvent.DitheringChange -> updateConfig { it.copy(enableDithering = event.enabled) }
            is ImageConfigEvent.TabSelectionChange -> _uiState.update { it.copy(selectedTabIndex = event.index) }
        }
    }

    fun loadImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                val safeBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                _uiState.update { it.copy(sourceBitmap = safeBitmap) }
                processImage()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun updateConfig(update: (PixelArtConfig) -> PixelArtConfig) {
        _uiState.update { it.copy(config = update(it.config)) }
        processImage()
    }

    private var processingJob: Job? = null

    private fun processImage() {
        val src = _uiState.value.sourceBitmap ?: return
        val currentConfig = _uiState.value.config
        
        processingJob?.cancel()
        processingJob = viewModelScope.launch {
            delay(100) // Debounce rapid slider/button changes
            if (!isActive) return@launch
            
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = converter.process(src, currentConfig)
                _uiState.update { it.copy(processedBitmap = result) }
            } catch (e: Exception) {
                 e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    suspend fun saveAndGetSpriteInfo(spriteName: String): Triple<String, Int, Int>? {
        val bitmap = _uiState.value.processedBitmap ?: return null
        val id = UUID.randomUUID().toString()
        val width = bitmap.width
        val height = bitmap.height

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val layer = Layer(
            id = UUID.randomUUID().toString(),
            name = "Layer 1",
            cel = Cel(
                x = 0,
                y = 0,
                width = width,
                height = height,
                pixels = pixels
            )
        )

        val sprite = Sprite(
            id = id,
            width = width,
            height = height,
            layers = listOf(layer)
        )

        spriteDatabaseRepository.saveSprite(sprite)
        return Triple(id, width, height)
    }
}
