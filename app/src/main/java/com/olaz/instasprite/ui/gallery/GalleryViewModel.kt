package com.olaz.instasprite.ui.gallery

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.domain.model.SpriteWithMeta
import com.olaz.instasprite.data.repository.SpriteDatabaseRepository
import com.olaz.instasprite.data.repository.SortSettingRepository
import com.olaz.instasprite.data.repository.StorageLocationRepository
import com.olaz.instasprite.domain.dialog.DialogController
import com.olaz.instasprite.domain.model.ColorPalette
import com.olaz.instasprite.domain.usecase.SaveFileUseCase
import com.olaz.instasprite.ui.gallery.contract.BottomBarEvent
import com.olaz.instasprite.ui.gallery.contract.ImagePagerEvent
import com.olaz.instasprite.ui.gallery.contract.SearchBarContract
import com.olaz.instasprite.ui.gallery.contract.SpriteListEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SpriteListOrder {
    Name,
    NameDesc,
    DateCreated,
    DateCreatedDesc,
    LastModified,
    LastModifiedDesc
}

data class GalleryState(
    val showSearchBar: Boolean = false,
    val showImagePager: Boolean = false
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val spriteDatabaseRepository: SpriteDatabaseRepository,
    private val sortSettingRepository: SortSettingRepository,
    private val storageLocationRepository: StorageLocationRepository,
    private val dialogController: DialogController<GalleryDialog>
) : ViewModel(),
    DialogController<GalleryDialog> by dialogController {
    private val saveFileUseCase = SaveFileUseCase()

    private val _uiState = MutableStateFlow(
        GalleryState()
    )
    val uiState: StateFlow<GalleryState> = _uiState.asStateFlow()

    val sprites: StateFlow<List<SpriteWithMeta>> =
        spriteDatabaseRepository.getAllSpritesWithMeta()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _lastSavedLocation = MutableStateFlow<Uri?>(null)
    val lastSavedLocation: StateFlow<Uri?> = _lastSavedLocation.asStateFlow()

    private val _spriteListOrder = MutableStateFlow(SpriteListOrder.DateCreatedDesc)
    val spriteListOrder = _spriteListOrder.asStateFlow()

    var lastEditedSpriteId by mutableStateOf<String?>(null)
    var currentSelectedSpriteIndex by mutableIntStateOf(0)
    var lastSpriteSeenInPager by mutableStateOf<Sprite?>(null)
    var selectedNewCanvasPalette by mutableStateOf<ColorPalette?>(null)

    var onOpenDrawing: (id: String, width: Int, height: Int, name: String?, palette: ColorPalette?) -> Unit = { _, _, _, _, _ -> }
    var onOpenPalette: () -> Unit = {}

    fun onCanvasPaletteSelected(palette: ColorPalette) {
        selectedNewCanvasPalette = palette
    }

    val sortedAndFilteredSprites: StateFlow<List<SpriteWithMeta>> = combine(
        sprites,
        _searchQuery,
        _spriteListOrder
    ) { list, query, order ->

        val filtered = if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.meta?.spriteName?.contains(query, ignoreCase = true) == true
            }
        }

        when (order) {
            SpriteListOrder.Name -> filtered.sortedBy { it.meta?.spriteName?.lowercase() ?: "" }
            SpriteListOrder.NameDesc -> filtered.sortedByDescending { it.meta?.spriteName?.lowercase() ?: "" }
            SpriteListOrder.DateCreated -> filtered.sortedBy { it.meta?.createdAt ?: 0L }
            SpriteListOrder.DateCreatedDesc -> filtered.sortedByDescending { it.meta?.createdAt ?: 0L }
            SpriteListOrder.LastModified -> filtered.sortedBy { it.meta?.lastModifiedAt ?: 0L }
            SpriteListOrder.LastModifiedDesc -> filtered.sortedByDescending { it.meta?.lastModifiedAt ?: 0L }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            sortSettingRepository.getLastSortSetting()?.let {
                _spriteListOrder.value = it
            }
        }
    }

    fun onBottomBarEvent(event: BottomBarEvent) {
        when (event) {
            is BottomBarEvent.ToggleSearchBar -> toggleSearchBar()
            is BottomBarEvent.OpenSelectSortOption -> openDialog(GalleryDialog.SelectSortOption)
        }
    }

    fun onImagePagerEvent(event: ImagePagerEvent) {
        when (event) {
            is ImagePagerEvent.OpenDeleteDialog -> openDialog(
                GalleryDialog.DeleteSpriteConfirm(
                    spriteName = event.spriteName,
                    spriteId = event.spriteId
                )
            )

            is ImagePagerEvent.OpenDrawingActivity -> onOpenDrawing(
                event.sprite.id,
                event.sprite.width,
                event.sprite.height,
                event.name,
                null
            )

            is ImagePagerEvent.OpenSaveImageDialog -> openDialog(GalleryDialog.SaveImage(event.sprite))
        }
    }

    fun onSearchBarEvent(event: SearchBarContract) {
        when (event) {
            is SearchBarContract.ToggleSearchBar -> toggleSearchBar()
            is SearchBarContract.UpdateSearchQuery -> updateSearchQuery(event.query)
        }
    }

    fun onSpriteListEvent(event: SpriteListEvent) {
        when (event) {
            is SpriteListEvent.OpenDeleteDialog ->
                openDialog(
                    GalleryDialog.DeleteSpriteConfirm(
                        spriteName = event.spriteName,
                        spriteId = event.spriteId
                    )
                )

            is SpriteListEvent.OpenDrawingScreen -> onOpenDrawing(
                event.sprite.id,
                event.sprite.width,
                event.sprite.height,
                event.name,
                null
            )

            is SpriteListEvent.OpenPager -> toggleImagePager(event.sprite)
            is SpriteListEvent.OpenRenameDialog -> openDialog(GalleryDialog.Rename(event.spriteId))
        }
    }

    fun toggleSearchBar() {
        _uiState.value = _uiState.value.copy(
            showSearchBar = !_uiState.value.showSearchBar
        )
    }

    fun toggleImagePager(selectedSprite: Sprite?) {
        _uiState.value = _uiState.value.copy(
            showImagePager = !_uiState.value.showImagePager
        )

        currentSelectedSpriteIndex = sortedAndFilteredSprites.value.indexOfFirst {
            it.sprite.id == selectedSprite?.id
        }
    }

    suspend fun getLastSavedLocation(): Uri? {
        _lastSavedLocation.value = storageLocationRepository.getLastSavedLocation()
        return _lastSavedLocation.value
    }

    fun setLastSavedLocation(uri: Uri) {
        _lastSavedLocation.value = uri
        viewModelScope.launch {
            storageLocationRepository.setLastSavedLocation(uri)
        }
    }

    fun saveImage(
        context: Context,
        sprite: Sprite,
        folderUri: Uri,
        fileName: String,
        scalePercent: Int = 100
    ): Boolean {
        val result = saveFileUseCase.saveImageFile(
            context,
            sprite,
            scalePercent,
            folderUri,
            fileName
        )

        result.fold(
            onSuccess = { return true },
            onFailure = { exception ->
                Log.e("SaveFile", "Failed to save file", exception)
                return false
            }
        )
    }

    fun deleteSpriteById(spriteId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                spriteDatabaseRepository.deleteSpriteById(spriteId)
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Error deleting sprite", e)
            }
        }
    }

    fun deleteSpriteByIdDelay(spriteId: String, duration: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            delay(duration)
            deleteSpriteById(spriteId)
        }
    }

    fun renameSprite(spriteId: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            spriteDatabaseRepository.changeName(spriteId, newName)
        }
    }

    fun saveSortSetting(spriteListOrder: SpriteListOrder) {
        viewModelScope.launch {
            sortSettingRepository.setLastSortSetting(spriteListOrder)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSpriteListOrder(order: SpriteListOrder) {
        _spriteListOrder.value = order
    }

    fun openColorPaletteScreen() {
        onOpenPalette()
    }
}