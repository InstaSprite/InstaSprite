package com.instasprite.app.ui.gallery

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasprite.app.data.repository.FileRepository
import com.instasprite.app.data.repository.SpriteDatabaseRepository
import com.instasprite.app.data.repository.StorageLocationRepository
import com.instasprite.app.domain.dialog.DialogController
import com.instasprite.app.domain.export.ImageExporter
import com.instasprite.app.domain.model.ColorPalette
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.model.SpriteWithMeta

import com.instasprite.app.ui.gallery.contract.ImagePagerEvent
import com.instasprite.app.ui.gallery.contract.SearchBarContract
import com.instasprite.app.ui.gallery.contract.SpriteListEvent
import com.instasprite.app.utils.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class SpriteListOrder {
    Name,
    NameDesc,
    DateCreated,
    DateCreatedDesc,
    LastModified,
    LastModifiedDesc
}

enum class GalleryLayoutMode { List, StaggeredGrid, SquareGrid }

data class GalleryState(
    val showSearchBar: Boolean = false,
    val showImagePager: Boolean = false,
    val layoutMode: GalleryLayoutMode = GalleryLayoutMode.List
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val spriteDatabaseRepository: SpriteDatabaseRepository,
    private val storageLocationRepository: StorageLocationRepository,
    private val fileRepository: FileRepository,
    private val dialogController: DialogController<GalleryDialog>,
    @ApplicationContext private val context: Context
) : ViewModel(),
    DialogController<GalleryDialog> by dialogController {

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
    var onOpenDrawing: (id: String, width: Int, height: Int, name: String?, paletteId: Int?) -> Unit = { _, _, _, _, _ -> }


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
        _spriteListOrder.value = AppSettings.getGallerySettings(context).sortOrder
        _uiState.value = _uiState.value.copy(
            layoutMode = AppSettings.getGallerySettings(context).layoutMode
        )
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

    fun setLayoutMode(mode: GalleryLayoutMode) {
        _uiState.update { it.copy(layoutMode = mode) }
        AppSettings.setGalleryLayout(context, mode)
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
        spriteId: String,
        folderUri: Uri,
        fileName: String,
        scalePercent: Int = 100,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val sprite = spriteDatabaseRepository.loadSprite(spriteId)
            if (sprite == null) {
                Log.e("SaveFile", "Sprite not found: $spriteId")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
                return@launch
            }
            val bitmap = ImageExporter.convertToBitmap(
                sprite.compositedPixels,
                sprite.width,
                sprite.height,
                scalePercent
            )
            
            val success = if (bitmap != null) {
                fileRepository.saveFile(bitmap, folderUri, fileName)
            } else false

            withContext(Dispatchers.Main) {
                if (success) {
                    onResult(true)
                } else {
                    Log.e("SaveFile", "Failed to save file")
                    onResult(false)
                }
            }
        }
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


    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSpriteListOrder(order: SpriteListOrder) {
        _spriteListOrder.value = order
        AppSettings.setGalleryOrder(context, order)
    }

    fun getSpriteDataFromFile(fileUri: Uri): Sprite? {
        return fileRepository.loadISpriteFile(fileUri)
    }

    fun importSprite(sprite: Sprite) {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = java.util.UUID.randomUUID().toString()
            val newSprite = sprite.copy(id = newId)
            
            spriteDatabaseRepository.saveSprite(newSprite)
            spriteDatabaseRepository.changeName(newId, "Imported Sprite")
            
            withContext(Dispatchers.Main) {
                onOpenDrawing(newId, newSprite.width, newSprite.height, "Imported Sprite", null)
            }
        }
    }
}