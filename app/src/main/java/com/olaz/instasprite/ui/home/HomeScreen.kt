package com.olaz.instasprite.ui.home

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.olaz.instasprite.ui.components.composable.JumpToTopButton
import com.olaz.instasprite.ui.gallery.GalleryDialog
import com.olaz.instasprite.ui.gallery.GalleryPageContent
import com.olaz.instasprite.ui.gallery.GalleryScreenDialogs
import com.olaz.instasprite.ui.gallery.GalleryScreenEvent
import com.olaz.instasprite.ui.gallery.GalleryViewModel
import com.olaz.instasprite.ui.gallery.component.ImagePagerOverlay
import com.olaz.instasprite.ui.gallery.component.SearchBar
import com.olaz.instasprite.ui.gallery.contract.BottomBarEvent
import com.olaz.instasprite.ui.home.component.FeedFab
import com.olaz.instasprite.ui.home.component.HomeBottomBar
import com.olaz.instasprite.ui.home.component.HomeDrawer
import com.olaz.instasprite.ui.home.component.HomeFab
import com.olaz.instasprite.ui.social.feed.FeedContent
import com.olaz.instasprite.ui.social.feed.FeedViewModel
import com.olaz.instasprite.ui.social.feed.contract.FeedContentState
import com.olaz.instasprite.ui.social.feed.contract.FeedScreenEvent
import com.olaz.instasprite.ui.social.session.SocialSessionState
import com.olaz.instasprite.ui.social.session.SocialSessionViewModel
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.utils.UiUtils
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

enum class HomeTab(val title: String) {
    GALLERY("Gallery"),
    FEED("Feed")
}

@Composable
fun HomeScreen(
    onNavigateToDrawing: (id: String, width: Int, height: Int, name: String?, paletteId: Int?) -> Unit,
    onNavigateToCreateCanvas: () -> Unit,
    onNavigateToLoadImage: () -> Unit,
    onLoginClick: () -> Unit,
    onOpenComments: (postId: Long) -> Unit,
    onOpenProfile: (userId: String) -> Unit,
    onOpenNotifications: () -> Unit,
    onNavigateToCreatePost: () -> Unit,
    galleryViewModel: GalleryViewModel = hiltViewModel(),
    feedViewModel: FeedViewModel = hiltViewModel(),
    sessionViewModel: SocialSessionViewModel = hiltViewModel(),
) {
    UiUtils.SetStatusBarColor(CatppuccinUI.TopBarColor)
    UiUtils.SetNavigationBarColor(CatppuccinUI.BottomBarColor)

    val tabs = HomeTab.entries.toTypedArray()
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val galleryUiState by galleryViewModel.uiState.collectAsState()
    val galleryDialogState by galleryViewModel.dialogState.collectAsState()
    val sortedSprites by galleryViewModel.sortedAndFilteredSprites.collectAsState()
    val searchQuery by galleryViewModel.searchQuery.collectAsState()
    val galleryListState = rememberLazyListState()

    val feedState by feedViewModel.contentState.collectAsState(initial = FeedContentState())
    val sessionState by sessionViewModel.sessionState.collectAsState()
    val feedListState = rememberLazyListState()
    val isLoggedIn = sessionState is SocialSessionState.LoggedIn
    val currentUsername = (sessionState as? SocialSessionState.LoggedIn)?.username
        ?.takeIf { it.isNotBlank() }
        ?: feedState.profileState.memberUsername.takeIf { it.isNotBlank() }


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

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(feedViewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            feedViewModel.startPolling()
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
            onBottomBarEvent = galleryViewModel::onBottomBarEvent,
            onImagePagerEvent = galleryViewModel::onImagePagerEvent,
            onSearchBarEvent = galleryViewModel::onSearchBarEvent,
            onSpriteListEvent = galleryViewModel::onSpriteListEvent,
            onCreateNewCanvas = onNavigateToCreateCanvas,
            onLoadCanvas = { galleryViewModel.openDialog(GalleryDialog.LoadISprite) },
            onLoadImage = onNavigateToLoadImage,
        )
    }

    val feedEvent = remember(feedViewModel, onLoginClick, onOpenComments, onOpenProfile) {
        FeedScreenEvent(
            onLoginClick = onLoginClick,
            onDismissVerifyEmailDialog = feedViewModel::dismissVerifyDialog,
            onVerifyEmail = feedViewModel::verifyEmail,
            onDismissPostFilterDialog = feedViewModel::togglePostFilterDialog,
            onSelectPostFilter = feedViewModel::setPostFilter,
            onOpenComments = onOpenComments,
            onOpenProfile = onOpenProfile,
            onToggleLike = feedViewModel::toggleLikePost,
            onToggleBookmark = feedViewModel::toggleBookmarkPost,
            onToggleFollow = feedViewModel::toggleFollow,
            onDeletePost = feedViewModel::deletePost,
            onRefreshed = feedViewModel::onRefreshed,
            onConsumeRefreshPending = feedViewModel::consumeRefreshPending,
            onUpdateTopPostId = feedViewModel::updateTopPostId
        )
    }

    val firstGalleryItemVisible by remember {
        derivedStateOf { galleryListState.firstVisibleItemIndex > 0 }
    }

    fun launchDrawerAction(action: () -> Unit = {}) {
        scope.launch {
            drawerState.close()
            action()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawer(
                isLoggedIn = isLoggedIn,
                profileState = feedState.profileState,
                profileImageState = feedState.profileImageState,
                username = currentUsername,
                onHomeClick = { launchDrawerAction() },
                onProfileClick = {
                    launchDrawerAction {
                        currentUsername?.let(onOpenProfile) ?: onLoginClick()
                    }
                },
                onLoginClick = { launchDrawerAction(onLoginClick) },
                onNotificationsClick = { launchDrawerAction(onOpenNotifications) },
                onSettingsClick = { launchDrawerAction() },
                onAboutClick = { launchDrawerAction() },
                onLogoutClick = {
                    launchDrawerAction {
                        sessionViewModel.logout()
                    }
                }
            )
        }
    ) {
        Box {
            Scaffold(
                topBar = {
                    SecondaryTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = CatppuccinUI.TopBarColor,
                        contentColor = CatppuccinUI.TextColorLight,
                        indicator = {
                            TabRowDefaults.SecondaryIndicator(
                                color = CatppuccinUI.SelectedColor
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
                                        text = tab.title,
                                        color = if (pagerState.currentPage == index)
                                            CatppuccinUI.TextColorLight
                                        else
                                            CatppuccinUI.Subtext0Color
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
                    HomeBottomBar(
                        onBottomBarEvent = if (pagerState.currentPage == 0)
                            galleryEvent.onBottomBarEvent
                        else
                            { _: BottomBarEvent -> },
                        onMenuClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        modifier = Modifier.height(56.dp)
                    )
                },
            ) { innerPadding ->
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                ) { page ->
                    when (page) {
                        0 -> GalleryPageContent(
                            uiState = galleryUiState,
                            lazyListState = galleryListState,
                            spriteList = sortedSprites,
                            searchQuery = searchQuery,
                            event = galleryEvent,
                            modifier = Modifier.fillMaxSize()
                        )

                        1 -> FeedContent(
                            isLoggedIn = isLoggedIn,
                            state = feedState,
                            listState = feedListState,
                            event = feedEvent,
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = firstGalleryItemVisible && pagerState.currentPage == 0,
                enter = scaleIn(),
                exit = scaleOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp)
            ) {
                JumpToTopButton(listState = galleryListState)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 21.dp)
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

                AnimatedVisibility(
                    visible = pagerState.currentPage == 1,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    FeedFab(
                        onCreatePost = onNavigateToCreatePost,
                    )
                }
            }
        }
    }
}




