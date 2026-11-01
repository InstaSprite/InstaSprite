package com.instasprite.app.ui.gallery.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instasprite.app.R
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.model.SpriteMeta
import com.instasprite.app.domain.model.SpriteWithMeta
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.composable.JumpToTopButton
import com.instasprite.app.ui.gallery.GalleryLayoutMode
import com.instasprite.app.ui.gallery.contract.SpriteListEvent
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.DummyData
import com.instasprite.app.utils.pixelDp
import kotlinx.coroutines.launch


@Composable
fun SpriteList(
    onSpriteListEvent: (SpriteListEvent) -> Unit,
    spriteList: List<SpriteWithMeta>,
    layoutMode: GalleryLayoutMode = GalleryLayoutMode.List,
    playingSpriteId: String? = null,
    playbackSprite: Sprite? = null,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    onOpenDrawing: (Sprite, String?) -> Unit,
) {
    val untitledLabel = stringResource(R.string.untitled)
    val gridState = rememberLazyGridState()
    val staggeredState = rememberLazyStaggeredGridState()
    val scope = rememberCoroutineScope()
    val isScrolled by remember(layoutMode, lazyListState) {
        derivedStateOf {
            when (layoutMode) {
                GalleryLayoutMode.List -> lazyListState.firstVisibleItemIndex
                GalleryLayoutMode.StaggeredGrid -> staggeredState.firstVisibleItemIndex
                GalleryLayoutMode.SquareGrid -> gridState.firstVisibleItemIndex
            } > 0
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = layoutMode,
            modifier = Modifier.fillMaxSize(),
            label = "LayoutTransition"
        ) { currentMode ->
            when (currentMode) {
                GalleryLayoutMode.List -> {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = spriteList,
                            key = { it.sprite.id }
                        ) { item ->
                            val (sprite, meta) = item
                            SpriteCard(
                                onDelete = {
                                    onSpriteListEvent(
                                        SpriteListEvent.OpenDeleteDialog(
                                            meta?.spriteName ?: untitledLabel,
                                            sprite.id
                                        )
                                    )
                                },
                                onRename = {
                                    onSpriteListEvent(SpriteListEvent.OpenRenameDialog(sprite.id))
                                },
                                onEdit = {
                                    onOpenDrawing(sprite, meta?.spriteName)
                                },
                                onClick = {
                                    onSpriteListEvent(SpriteListEvent.OpenPager(sprite))
                                },
                                sprite = sprite,
                                meta = meta,
                                hasAnimation = item.hasAnimation,
                                playing = playingSpriteId == sprite.id,
                                playbackSprite = playbackSprite?.takeIf { it.id == sprite.id },
                                onPlay = { onSpriteListEvent(SpriteListEvent.TogglePlayback(sprite.id)) },
                                onStop = { onSpriteListEvent(SpriteListEvent.StopPlayback(sprite.id)) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }

                GalleryLayoutMode.StaggeredGrid -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        state = staggeredState,
                        horizontalArrangement = Arrangement.spacedBy(6.pixelDp),
                        verticalItemSpacing = 6.pixelDp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = spriteList,
                            key = { it.sprite.id }
                        ) { spriteWithMeta ->
                            val (sprite, meta) = spriteWithMeta
                            SpriteGridCard(
                                spriteWithMeta = spriteWithMeta,
                                playing = playingSpriteId == sprite.id,
                                playbackSprite = playbackSprite?.takeIf { it.id == sprite.id },
                                onPlay = { onSpriteListEvent(SpriteListEvent.TogglePlayback(sprite.id)) },
                                onStop = { onSpriteListEvent(SpriteListEvent.StopPlayback(sprite.id)) },
                                layoutMode = GalleryLayoutMode.StaggeredGrid,
                                onEdit = { onOpenDrawing(sprite, meta?.spriteName) },
                                onRename = { onSpriteListEvent(SpriteListEvent.OpenRenameDialog(sprite.id)) },
                                onDelete = {
                                    onSpriteListEvent(
                                        SpriteListEvent.OpenDeleteDialog(
                                            meta?.spriteName ?: untitledLabel,
                                            sprite.id
                                        )
                                    )
                                },
                                onClick = { onSpriteListEvent(SpriteListEvent.OpenPager(sprite)) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }

                GalleryLayoutMode.SquareGrid -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        horizontalArrangement = Arrangement.spacedBy(6.pixelDp),
                        verticalArrangement = Arrangement.spacedBy(6.pixelDp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = spriteList,
                            key = { it.sprite.id }
                        ) { spriteWithMeta ->
                            val (sprite, meta) = spriteWithMeta
                            SpriteGridCard(
                                spriteWithMeta = spriteWithMeta,
                                playing = playingSpriteId == sprite.id,
                                playbackSprite = playbackSprite?.takeIf { it.id == sprite.id },
                                onPlay = { onSpriteListEvent(SpriteListEvent.TogglePlayback(sprite.id)) },
                                onStop = { onSpriteListEvent(SpriteListEvent.StopPlayback(sprite.id)) },
                                layoutMode = GalleryLayoutMode.SquareGrid,
                                onEdit = { onOpenDrawing(sprite, meta?.spriteName) },
                                onRename = { onSpriteListEvent(SpriteListEvent.OpenRenameDialog(sprite.id)) },
                                onDelete = {
                                    onSpriteListEvent(
                                        SpriteListEvent.OpenDeleteDialog(
                                            meta?.spriteName ?: untitledLabel,
                                            sprite.id
                                        )
                                    )
                                },
                                onClick = { onSpriteListEvent(SpriteListEvent.OpenPager(sprite)) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = isScrolled,
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 6.pixelDp)
        ) {
            JumpToTopButton(onClick = {
                scope.launch {
                    when (layoutMode) {
                        GalleryLayoutMode.List -> {
                            if (lazyListState.firstVisibleItemIndex > 10) lazyListState.scrollToItem(0)
                            else lazyListState.animateScrollToItem(0)
                        }
                        GalleryLayoutMode.StaggeredGrid -> {
                            if (staggeredState.firstVisibleItemIndex > 10) staggeredState.scrollToItem(0)
                            else staggeredState.animateScrollToItem(0)
                        }
                        GalleryLayoutMode.SquareGrid -> {
                            if (gridState.firstVisibleItemIndex > 10) gridState.scrollToItem(0)
                            else gridState.animateScrollToItem(0)
                        }
                    }
                }
            })
        }
    }
}

@Composable
private fun SpriteCard(
    onDelete: () -> Unit = {},
    onRename: () -> Unit = {},
    onEdit: () -> Unit = {},
    onClick: () -> Unit = {},
    sprite: Sprite,
    meta: SpriteMeta?,
    hasAnimation: Boolean = false,
    playing: Boolean = false,
    playbackSprite: Sprite? = null,
    onPlay: () -> Unit = {},
    onStop: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showDropdown by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.background,
            ),
            modifier = Modifier
                .padding(vertical = 6.pixelDp)
                .combinedClickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {},
                    onLongClick = { showDropdown = true }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.pixelDp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    if (hasAnimation) SpritePlaybackButton(playing, onPlay)
                    Text(
                        text = meta?.spriteName ?: stringResource(R.string.untitled),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showDropdown = true },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(26.pixelDp)
                    ) {
                        PixelIcon(
                            icon = R.drawable.ic_three_dots,
                            contentDescription = stringResource(R.string.options),
                            tint = AppTheme.colors.onSurface
                        )
                    }

                    SpriteDropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        onDelete = {
                            onDelete()
                            showDropdown = false
                        },
                        onEdit = {
                            onEdit()
                            showDropdown = false
                        },
                        onRename = {
                            onRename()
                            showDropdown = false
                        }
                    )
                }
            }

            GallerySpritePreview(
                playing = playing,
                playbackSprite = playbackSprite,
                onStop = onStop,
                sprite = sprite,
                meta = meta,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .align(Alignment.CenterHorizontally)
                    .clip(MaterialTheme.shapes.small),
                onClick = onClick
            )

            Spacer(modifier = Modifier.height(10.pixelDp))
        }
    }
}

@Composable
internal fun SpriteDropdownMenu(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    onRename: () -> Unit = {},
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.colors.surfaceContainer,
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.rename)) },
            trailingIcon = {
                PixelIcon(
                    icon = R.drawable.ic_info,
                    contentDescription = stringResource(R.string.rename),
                    tint = AppTheme.colors.onSurface,
                )
            },
            onClick = onRename,
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.edit)) },
            trailingIcon = {
                PixelIcon(
                    icon = R.drawable.ic_edit,
                    contentDescription = stringResource(R.string.edit),
                    tint = AppTheme.colors.onSurface
                )
            },
            onClick = onEdit,
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.delete)) },
            trailingIcon = {
                PixelIcon(
                    icon = R.drawable.ic_trash,
                    contentDescription = stringResource(R.string.delete),
                    tint = AppTheme.colors.onSurface
                )
            },
            onClick = onDelete,
        )
    }
}

@Preview(showBackground = true, name = "SpriteList List", heightDp = 600)
@Composable
private fun SpriteListListPreview() {
    InstaSpriteTheme {
        SpriteList(
            onSpriteListEvent = {},
            onOpenDrawing = { _, _ -> },
            spriteList = DummyData.previewSprites,
            layoutMode = GalleryLayoutMode.List
        )
    }
}

@Preview(showBackground = true, name = "SpriteList Staggered", heightDp = 600)
@Composable
private fun SpriteListStaggeredPreview() {
    InstaSpriteTheme {
        SpriteList(
            onSpriteListEvent = {},
            onOpenDrawing = { _, _ -> },
            spriteList = DummyData.previewSprites,
            layoutMode = GalleryLayoutMode.StaggeredGrid
        )
    }
}

@Preview(showBackground = true, name = "SpriteList Square Grid", heightDp = 600)
@Composable
private fun SpriteListSquareGridPreview() {
    InstaSpriteTheme {
        SpriteList(
            onSpriteListEvent = {},
            onOpenDrawing = { _, _ -> },
            spriteList = DummyData.previewSprites,
            layoutMode = GalleryLayoutMode.SquareGrid
        )
    }
}
