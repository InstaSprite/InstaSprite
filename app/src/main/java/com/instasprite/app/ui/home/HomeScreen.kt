package com.instasprite.app.ui.home

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instasprite.app.R
import com.instasprite.app.ui.gallery.GalleryDialog
import com.instasprite.app.ui.gallery.GalleryPageContent
import com.instasprite.app.ui.gallery.GalleryScreenDialogs
import com.instasprite.app.ui.gallery.GalleryScreenEvent
import com.instasprite.app.ui.gallery.GalleryViewModel
import com.instasprite.app.ui.gallery.component.ImagePagerOverlay
import com.instasprite.app.ui.gallery.component.SearchBar
import com.instasprite.app.ui.home.component.BottomBarItem
import com.instasprite.app.ui.home.component.HomeBottomBar
import com.instasprite.app.ui.home.component.HomeDrawer
import com.instasprite.app.ui.home.component.HomeFab
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.UiUtils
import com.instasprite.app.utils.pixelDp
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

enum class HomeTab(@StringRes val titleRes: Int) {
    GALLERY(R.string.gallery),
}

@Composable
fun HomeScreen(
    onNavigateToDrawing: (id: String, width: Int, height: Int, name: String?, paletteId: Int?) -> Unit,
    onNavigateToCreateCanvas: () -> Unit,
    onNavigateToLoadImage: () -> Unit,
    onNavigateToPalette: () -> Unit,
    onOpenSetting: () -> Unit,
    onOpenAbout: () -> Unit,
    galleryViewModel: GalleryViewModel = hiltViewModel()
) {
    UiUtils.SetStatusBarColor(AppTheme.colors.TopBarColor)
    UiUtils.SetNavigationBarColor(AppTheme.colors.BottomBarColor)

    val tabs = HomeTab.entries.toTypedArray()
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val galleryUiState by galleryViewModel.uiState.collectAsState()
    val galleryDialogState by galleryViewModel.dialogState.collectAsState()
    val sortedSprites by galleryViewModel.sortedAndFilteredSprites.collectAsState()
    val searchQuery by galleryViewModel.searchQuery.collectAsState()
    val galleryListState = rememberLazyListState()

    LaunchedEffect(galleryViewModel.lastEditedSpriteId) {
        val editedId = galleryViewModel.lastEditedSpriteId ?: return@LaunchedEffect
        snapshotFlow { sortedSprites }.collect { list ->
            val index = list.indexOfFirst { it.sprite.id == editedId }
            if (index != -1) {
                galleryListState.scrollToItem(index)
                galleryViewModel.lastEditedSpriteId = null
                cancel()
            }
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) galleryListState.scrollToItem(0)
    }

    LaunchedEffect(galleryViewModel.lastSpriteSeenInPager) {
        galleryViewModel.lastSpriteSeenInPager?.let { sprite ->
            val index = sortedSprites.indexOfFirst { it.sprite.id == sprite.id }
            if (index != -1) scope.launch { galleryListState.animateScrollToItem(index) }
            galleryViewModel.lastSpriteSeenInPager = null
        }
    }

    if (galleryUiState.showImagePager) {
        ImagePagerOverlay(
            onImagePagerEvent = galleryViewModel::onImagePagerEvent,
            spriteList = sortedSprites,
            startIndex = galleryViewModel.currentSelectedSpriteIndex,
            onDismiss = { lastSpriteSeen ->
                galleryViewModel.toggleImagePager(null)
                galleryViewModel.lastSpriteSeenInPager = lastSpriteSeen
            }
        )
    }

    GalleryScreenDialogs(galleryDialogState, galleryViewModel)

    val galleryEvent = remember(galleryViewModel) {
        galleryViewModel.onOpenDrawing = onNavigateToDrawing
        GalleryScreenEvent(
            onImagePagerEvent = galleryViewModel::onImagePagerEvent,
            onSearchBarEvent = galleryViewModel::onSearchBarEvent,
            onSpriteListEvent = galleryViewModel::onSpriteListEvent,
            onCreateNewCanvas = onNavigateToCreateCanvas,
            onLoadCanvas = { galleryViewModel.openDialog(GalleryDialog.LoadISprite) },
            onLoadImage = onNavigateToLoadImage,
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawer(
                onSettingsClick = onOpenSetting,
                onAboutClick = onOpenAbout,
                onBackClick = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Box {
            Scaffold(
                topBar = {
                    PrimaryTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = AppTheme.colors.TopBarColor,
                        contentColor = AppTheme.colors.TextColorLight,
                        indicator = {
                            TabRowDefaults.SecondaryIndicator(
                                color = AppTheme.colors.SelectedColor,
                                modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),

                                )
                        },
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                text = {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(tab.titleRes),
                                        color = if (pagerState.currentPage == index)
                                            AppTheme.colors.TextColorLight
                                        else
                                            AppTheme.colors.Subtext0Color
                                    )
                                }
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = galleryUiState.showSearchBar && pagerState.currentPage == 0,
                        enter = slideInVertically(initialOffsetY = { -it }),
                        exit = slideOutVertically(targetOffsetY = { -it }),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        SearchBar(
                            onSearchBarEvent = galleryEvent.onSearchBarEvent,
                            searchQuery = searchQuery,
                        )
                    }
                },
                bottomBar = {
                    HomeScreenBottomBar(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onPaletteClick = onNavigateToPalette,
                        onSearchClick = {
                            galleryViewModel.toggleSearchBar()
                        },
                        onSortClick = {
                            galleryViewModel.openDialog(GalleryDialog.DisplayOptions)
                        }
                    )
                },
            ) { innerPadding ->
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) { page ->
                    when (page) {
                        0 -> GalleryPageContent(
                            uiState = galleryUiState,
                            lazyListState = galleryListState,
                            spriteList = sortedSprites,
                            searchQuery = searchQuery,
                            event = galleryEvent,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.pixelDp)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedVisibility(
                    visible = pagerState.currentPage == 0,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    HomeFab(
                        onCreateCanvas = galleryEvent.onCreateNewCanvas,
                        onLoadCanvas = galleryEvent.onLoadCanvas,
                        onLoadImage = galleryEvent.onLoadImage,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreenBottomBar(
    onMenuClick: () -> Unit,
    onPaletteClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSortClick: () -> Unit
) {
    HomeBottomBar(
        leftItems = {
            BottomBarItem(
                icon = R.drawable.ic_menu,
                title = stringResource(R.string.menu),
                onClick = onMenuClick,
                iconTint = AppTheme.colors.TextColorLight,
            )
            BottomBarItem(
                icon = R.drawable.ic_palette,
                title = stringResource(R.string.palette),
                onClick = onPaletteClick,
                iconTint = AppTheme.colors.TextColorLight,
            )
        },
        rightItems = {
            BottomBarItem(
                icon = R.drawable.ic_search,
                title = stringResource(R.string.search),
                onClick = onSearchClick,
                iconTint = AppTheme.colors.TextColorLight
            )
            BottomBarItem(
                icon = R.drawable.ic_sort,
                title = stringResource(R.string.sort),
                onClick = onSortClick,
                iconTint = AppTheme.colors.TextColorLight
            )
        },
        modifier = Modifier.height(48.pixelDp)
    )
}
