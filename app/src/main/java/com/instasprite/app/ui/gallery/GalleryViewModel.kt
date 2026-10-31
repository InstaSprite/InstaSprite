package com.instasprite.app.ui.gallery

import com.instasprite.app.data.diagnostics.CrashReports
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasprite.app.R
import com.instasprite.app.data.repository.FileRepository
import com.instasprite.app.data.repository.SpriteDatabaseRepository
import com.instasprite.app.data.repository.StorageLocationRepository
import com.instasprite.app.di.IoDispatcher
import com.instasprite.app.domain.dialog.DialogController
import com.instasprite.app.domain.export.AnimationExportFormat
import com.instasprite.app.domain.model.Animation
import com.instasprite.app.ui.gallery.contract.GalleryExportFormat
import kotlinx.coroutines.ensureActive
import com.instasprite.app.domain.export.ImageExporter
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.model.SpriteWithMeta
import com.instasprite.app.ui.gallery.contract.ImagePagerEvent
import com.instasprite.app.ui.gallery.contract.SearchBarContract
import com.instasprite.app.ui.gallery.contract.SpriteListEvent
import com.instasprite.app.utils.AppSettings
import com.instasprite.app.utils.CanvasMemoryException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineDispatcher
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

data class DrawingDestination(
    val spriteId: String,
    val width: Int,
    val height: Int,
    val name: String?,
    val paletteId: Int? = null
)

data class GalleryState(
    val showSearchBar: Boolean = false,
    val showImagePager: Boolean = false,
    val layoutMode: GalleryLayoutMode = GalleryLayoutMode.List,
    val drawingDestination: DrawingDestination? = null,
    val playingSpriteId: String? = null,
    val playbackSprite: Sprite? = null,
    val isExporting: Boolean = false
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val spriteDatabaseRepository: SpriteDatabaseRepository,
    private val storageLocationRepository: StorageLocationRepository,
    private val fileRepository: FileRepository,
    private val dialogController: DialogController<GalleryDialog>,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
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
            SpriteListOrder.NameDesc -> filtered.sortedByDescending {
                it.meta?.spriteName?.lowercase() ?: ""
            }

            SpriteListOrder.DateCreated -> filtered.sortedBy { it.meta?.createdAt ?: 0L }
            SpriteListOrder.DateCreatedDesc -> filtered.sortedByDescending {
                it.meta?.createdAt ?: 0L
            }

            SpriteListOrder.LastModified -> filtered.sortedBy { it.meta?.lastModifiedAt ?: 0L }
            SpriteListOrder.LastModifiedDesc -> filtered.sortedByDescending {
                it.meta?.lastModifiedAt ?: 0L
            }
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
            is ImagePagerEvent.TogglePlayback -> togglePreview(event.spriteId)
            is ImagePagerEvent.StopPlayback -> {
                if (_uiState.value.playingSpriteId == event.spriteId) stopPreview()
            }
            is ImagePagerEvent.OpenDeleteDialog -> openDialog(
                GalleryDialog.DeleteSpriteConfirm(
                    spriteName = event.spriteName,
                    spriteId = event.spriteId
                )
            )

            is ImagePagerEvent.Export -> {
                stopPreview()
                viewModelScope.launch { prepareExport(event.sprite, event.format) }
            }
        }
    }

    fun onSearchBarEvent(event: SearchBarContract) {
        when (event) {
            is SearchBarContract.ToggleSearchBar -> toggleSearchBar()
            is SearchBarContract.UpdateSearchQuery -> updateSearchQuery(event.query)
        }
    }

    private var playbackLoadJob: Job? = null

    fun stopPreview() {
        playbackLoadJob?.cancel()
        playbackLoadJob = null
        _uiState.update { it.copy(playingSpriteId = null, playbackSprite = null) }
    }

    private fun togglePreview(spriteId: String) {
        val stop = _uiState.value.playingSpriteId == spriteId
        stopPreview()
        if (stop) return
        _uiState.update { it.copy(playingSpriteId = spriteId) }
        playbackLoadJob = viewModelScope.launch {
            try {
                val sprite = withContext(ioDispatcher) {
                    spriteDatabaseRepository.loadSprite(spriteId)
                }
                requireNotNull(sprite)
                require((sprite.animation?.frames?.size ?: 0) > 1)
                _uiState.update { it.copy(playbackSprite = sprite) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("GalleryViewModel", "Failed to load animation", e)
                _uiState.update { it.copy(playingSpriteId = null, playbackSprite = null) }
                Toast.makeText(context, R.string.error_action_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onSpriteListEvent(event: SpriteListEvent) {
        when (event) {
            is SpriteListEvent.TogglePlayback -> togglePreview(event.spriteId)
            is SpriteListEvent.StopPlayback -> {
                if (_uiState.value.playingSpriteId == event.spriteId) stopPreview()
            }
            is SpriteListEvent.OpenDeleteDialog ->
                openDialog(
                    GalleryDialog.DeleteSpriteConfirm(
                        spriteName = event.spriteName,
                        spriteId = event.spriteId
                    )
                )

            is SpriteListEvent.OpenPager -> {
                stopPreview()
                toggleImagePager(event.sprite)
            }
            is SpriteListEvent.OpenRenameDialog -> openDialog(GalleryDialog.Rename(event.spriteId))
        }
    }

    fun toggleSearchBar() {
        _uiState.value = _uiState.value.copy(
            showSearchBar = !_uiState.value.showSearchBar
        )
    }

    private fun openDrawing(
        spriteId: String,
        width: Int,
        height: Int,
        name: String?,
        paletteId: Int? = null
    ) {
        _uiState.update {
            it.copy(
                drawingDestination = DrawingDestination(
                    spriteId,
                    width,
                    height,
                    name,
                    paletteId
                )
            )
        }
    }

    fun onDrawingNavigationHandled(destination: DrawingDestination) {
        _uiState.update {
            if (it.drawingDestination == destination) it.copy(drawingDestination = null) else it
        }
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

    suspend fun prepareExport(item: SpriteWithMeta, format: GalleryExportFormat) {
        if (_uiState.value.isExporting || (format == GalleryExportFormat.SPRITE_SHEET && !item.hasAnimation)) return
        _uiState.update { it.copy(isExporting = true) }
        try {
            val sprite = withContext(ioDispatcher) {
                requireNotNull(spriteDatabaseRepository.loadSprite(item.sprite.id))
            }
            require(format != GalleryExportFormat.SPRITE_SHEET || (sprite.animation?.frames?.size ?: 0) > 1)
            openDialog(GalleryDialog.ExportFile(sprite, item.meta?.spriteName.orEmpty(), format))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            CrashReports.record("prepare_export", e)
            Log.e("GalleryViewModel", "Failed to prepare export", e)
            Toast.makeText(context, R.string.export_failed, Toast.LENGTH_SHORT).show()
        } finally {
            _uiState.update { it.copy(isExporting = false) }
        }
    }

    suspend fun exportSprite(
        sprite: Sprite, format: GalleryExportFormat, folderUri: Uri,
        fileName: String, scalePercent: Int = 100, columns: Int = 1
    ): Boolean {
        if (_uiState.value.isExporting || fileName.isBlank() ||
            (format == GalleryExportFormat.SPRITE_SHEET && (sprite.animation?.frames?.size ?: 0) < 2)
        ) return false
        _uiState.update { it.copy(isExporting = true) }
        return try {
            withContext(ioDispatcher) {
                when (format) {
                    GalleryExportFormat.IMAGE -> {
                        val bitmap = ImageExporter.convertToBitmap(sprite, scalePercent = scalePercent)
                            ?: return@withContext false
                        try {
                            ensureActive()
                            fileRepository.saveFile(bitmap, folderUri, fileName)
                        } finally {
                            bitmap.recycle()
                        }
                    }
                    GalleryExportFormat.PROJECT -> fileRepository.saveISpriteFile(sprite, folderUri, fileName)
                    GalleryExportFormat.GIF, GalleryExportFormat.SPRITE_SHEET -> {
                        val animation = sprite.animation ?: Animation.fromLayers(sprite.layers)
                        fileRepository.saveAnimationFile(
                            sprite.copy(animation = animation),
                            if (format == GalleryExportFormat.GIF) AnimationExportFormat.GIF else AnimationExportFormat.SPRITE_SHEET,
                            folderUri, fileName, columns, scalePercent
                        )
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            CrashReports.record("export", e)
            Log.e("GalleryViewModel", "Failed to export sprite", e)
            if (e is CanvasMemoryException) {
                Toast.makeText(context, R.string.operation_memory_limit, Toast.LENGTH_LONG).show()
            }
            false
        } finally {
            _uiState.update { it.copy(isExporting = false) }
        }
    }

    fun deleteSpriteById(spriteId: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                spriteDatabaseRepository.deleteSpriteById(spriteId)
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Error deleting sprite", e)
            }
        }
    }

    fun deleteSpriteByIdDelay(spriteId: String, duration: Long) {
        viewModelScope.launch(ioDispatcher) {
            delay(duration)
            deleteSpriteById(spriteId)
        }
    }

    fun renameSprite(spriteId: String, newName: String) {
        viewModelScope.launch(ioDispatcher) {
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
        viewModelScope.launch(ioDispatcher) {
            val newId = java.util.UUID.randomUUID().toString()
            val newSprite = sprite.copy(id = newId)

            spriteDatabaseRepository.saveSprite(newSprite)
            spriteDatabaseRepository.changeName(newId, "Imported Sprite")

            withContext(Dispatchers.Main) {
                openDrawing(newId, newSprite.width, newSprite.height, "Imported Sprite")
            }
        }
    }
}
