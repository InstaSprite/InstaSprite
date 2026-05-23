package com.instasprite.app.ui.gallery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instasprite.app.R
import com.instasprite.app.domain.model.SpriteWithMeta
import com.instasprite.app.ui.components.composable.JumpToTopButton
import com.instasprite.app.ui.gallery.component.ImagePagerOverlay
import com.instasprite.app.ui.gallery.component.SearchBar
import com.instasprite.app.ui.gallery.component.SpriteList
import com.instasprite.app.ui.gallery.contract.BottomBarEvent
import com.instasprite.app.ui.gallery.contract.ImagePagerEvent
import com.instasprite.app.ui.gallery.contract.SearchBarContract
import com.instasprite.app.ui.gallery.contract.SpriteListEvent
import com.instasprite.app.ui.home.component.HomeBottomBar
import com.instasprite.app.ui.home.component.HomeFab
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.DummyData
import com.instasprite.app.utils.UiUtils
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

data class GalleryScreenEvent(
    val onBottomBarEvent: (BottomBarEvent) -> Unit,
    val onImagePagerEvent: (ImagePagerEvent) -> Unit,
    val onSearchBarEvent: (SearchBarContract) -> Unit,
    val onSpriteListEvent: (SpriteListEvent) -> Unit,
    val onCreateNewCanvas: () -> Unit,
    val onLoadCanvas: () -> Unit,
    val onLoadImage: () -> Unit,
)

@Composable
fun GalleryScreen(
    onNavigateToDrawing: (id: String, width: Int, height: Int, name: String?, paletteId: Int?) -> Unit,
    onNavigateToPalette: () -> Unit,
    onNavigateToCreateCanvas: () -> Unit,
    onNavigateToLoadImage: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    UiUtils.SetStatusBarColor(AppTheme.colors.TopBarColor)
    UiUtils.SetNavigationBarColor(AppTheme.colors.BottomBarColor)

    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val sprites by viewModel.sprites.collectAsState()
    val sortedSprites by viewModel.sortedAndFilteredSprites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val scope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()

    LaunchedEffect(viewModel.lastEditedSpriteId) {
        val editedId = viewModel.lastEditedSpriteId ?: return@LaunchedEffect

        snapshotFlow { sortedSprites }
            .collect { list ->
                val index = list.indexOfFirst { it.sprite.id == editedId }
                if (index != -1) {
                    lazyListState.scrollToItem(index)
                    viewModel.lastEditedSpriteId = null
                    cancel()
                }
            }
    }


    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            lazyListState.scrollToItem(0)
        }
    }

    LaunchedEffect(viewModel.lastSpriteSeenInPager) {
        viewModel.lastSpriteSeenInPager?.let { sprite ->
            val index = sortedSprites.indexOfFirst { it.sprite.id == sprite.id }
            if (index != -1) {
                scope.launch {
                    lazyListState.animateScrollToItem(index)
                }
            }
            viewModel.lastSpriteSeenInPager = null
        }
    }

    if (uiState.showImagePager) {
        ImagePagerOverlay(
            onImagePagerEvent = viewModel::onImagePagerEvent,
            spriteList = sortedSprites,
            startIndex = viewModel.currentSelectedSpriteIndex,
            onDismiss = { lastSpriteSeen ->
                viewModel.toggleImagePager(null)
                viewModel.lastSpriteSeenInPager = lastSpriteSeen
            }
        )
    }

    GalleryScreenDialogs(dialogState, viewModel)

    val event = remember(viewModel) {
        viewModel.onOpenDrawing = onNavigateToDrawing

        GalleryScreenEvent(
            onBottomBarEvent = viewModel::onBottomBarEvent,
            onImagePagerEvent = viewModel::onImagePagerEvent,
            onSearchBarEvent = viewModel::onSearchBarEvent,
            onSpriteListEvent = viewModel::onSpriteListEvent,
            onCreateNewCanvas = onNavigateToCreateCanvas,
            onLoadCanvas = { viewModel.openDialog(GalleryDialog.LoadISprite) },
            onLoadImage = onNavigateToLoadImage,
        )
    }

    GalleryScreenContent(
        uiState = uiState,
        lazyListState = lazyListState,
        spriteList = sortedSprites,
        searchQuery = searchQuery,
        event = event
    )
}

@Composable
fun GalleryPageContent(
    uiState: GalleryState,
    lazyListState: LazyListState,
    spriteList: List<SpriteWithMeta>,
    searchQuery: String,
    event: GalleryScreenEvent,
    modifier: Modifier = Modifier,
) {

    var isScrolled by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.BackgroundColorDarker)
            .animateContentSize()
    ) {
        if (spriteList.isNotEmpty()) {
            SpriteList(
                onSpriteListEvent = event.onSpriteListEvent,
                spriteList = spriteList,
                layoutMode = uiState.layoutMode,
                onIsScrolledChange = { isScrolled = it },
                lazyListState = lazyListState,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            AnimatedVisibility(
                visible = isScrolled,
                enter = scaleIn(),
                exit = scaleOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            ) {
                JumpToTopButton(listState = lazyListState)
            }
        } else {
            Text(
                text = stringResource(R.string.click_plus_to_create),
                modifier = Modifier.align(Alignment.Center)
            )
        }

    }
}

@Composable
private fun GalleryScreenContent(
    uiState: GalleryState,
    lazyListState: LazyListState,
    spriteList: List<SpriteWithMeta>,
    searchQuery: String,
    event: GalleryScreenEvent,
) {
    var isScrolled by remember { mutableStateOf(false) }

    Box {
        Scaffold(
            topBar = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(AppTheme.colors.TopBarColor)
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp)
                    )
                }

                AnimatedVisibility(
                    visible = uiState.showSearchBar,
                    enter = slideInVertically(initialOffsetY = { -it }),
                    exit = slideOutVertically(targetOffsetY = { -it }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    SearchBar(
                        onSearchBarEvent = event.onSearchBarEvent,
                        searchQuery = searchQuery,
                    )
                }
            },
            bottomBar = {
                HomeBottomBar(
                    onBottomBarEvent = event.onBottomBarEvent,
                    modifier = Modifier.height(56.dp)
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(AppTheme.colors.BackgroundColorDarker)
                    .animateContentSize()
            ) {
                SpriteList(
                    onSpriteListEvent = event.onSpriteListEvent,
                    spriteList = spriteList,
                    layoutMode = uiState.layoutMode,
                    onIsScrolledChange = { isScrolled = it },
                    lazyListState = lazyListState,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isScrolled,
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
        ) {
            JumpToTopButton(
                listState = lazyListState
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 21.dp)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            HomeFab(
                onCreateCanvas = event.onCreateNewCanvas,
                onLoadCanvas = event.onLoadCanvas,
                onLoadImage = event.onLoadImage,
            )
        }
    }
}

@Preview
@Composable
private fun GalleryScreenPreview() {
    InstaSpriteTheme {
        GalleryScreenContent(
            uiState = GalleryState(),
            lazyListState = LazyListState(),
            spriteList = DummyData.previewSprites,
            searchQuery = "",
            event = GalleryScreenEvent(
                onBottomBarEvent = {},
                onImagePagerEvent = {},
                onSearchBarEvent = {},
                onSpriteListEvent = {},
                onCreateNewCanvas = {},
                onLoadCanvas = {},
                onLoadImage = {},
            )
        )
    }
}

@Preview
@Composable
private fun GalleryScreenPreviewStaggeredLayout() {
    InstaSpriteTheme {
        GalleryScreenContent(
            uiState = GalleryState(layoutMode = GalleryLayoutMode.StaggeredGrid),
            lazyListState = LazyListState(),
            spriteList = DummyData.previewSprites,
            searchQuery = "",
            event = GalleryScreenEvent(
                onBottomBarEvent = {},
                onImagePagerEvent = {},
                onSearchBarEvent = {},
                onSpriteListEvent = {},
                onCreateNewCanvas = {},
                onLoadCanvas = {},
                onLoadImage = {},
            )
        )
    }
}

@Preview
@Composable
private fun GalleryScreenPreviewSquareLayout() {
    InstaSpriteTheme {
        GalleryScreenContent(
            uiState = GalleryState(layoutMode = GalleryLayoutMode.SquareGrid),
            lazyListState = LazyListState(),
            spriteList = DummyData.previewSprites,
            searchQuery = "",
            event = GalleryScreenEvent(
                onBottomBarEvent = {},
                onImagePagerEvent = {},
                onSearchBarEvent = {},
                onSpriteListEvent = {},
                onCreateNewCanvas = {},
                onLoadCanvas = {},
                onLoadImage = {},
            )
        )
    }
}