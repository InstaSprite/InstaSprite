package com.instasprite.app.ui.gallery.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.model.SpriteMeta
import com.instasprite.app.domain.model.SpriteWithMeta
import com.instasprite.app.ui.components.composable.AsyncCanvasPreviewer
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.gallery.GalleryLayoutMode
import com.instasprite.app.ui.gallery.contract.SpriteListEvent
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.DummyData


@Composable
fun SpriteList(
    onSpriteListEvent: (SpriteListEvent) -> Unit,
    spriteList: List<SpriteWithMeta>,
    layoutMode: GalleryLayoutMode = GalleryLayoutMode.List,
    onIsScrolledChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val context = LocalContext.current

    AnimatedContent(
        targetState = layoutMode,
        label = "LayoutTransition"
    ) { currentMode ->
        when (currentMode) {
            GalleryLayoutMode.List -> {
                val isScrolled by remember { derivedStateOf { lazyListState.firstVisibleItemIndex > 0 } }
                LaunchedEffect(isScrolled) { onIsScrolledChange(isScrolled) }

                LazyColumn(
                    state = lazyListState,
                    modifier = modifier
                ) {
                    items(
                        items = spriteList,
                        key = { it.sprite.id }
                    ) { (sprite, meta) ->
                        SpriteCard(
                            onDelete = {
                                onSpriteListEvent(SpriteListEvent.OpenDeleteDialog(meta!!.spriteName, sprite.id))
                            },
                            onRename = {
                                onSpriteListEvent(SpriteListEvent.OpenRenameDialog(sprite.id))
                            },
                            onEdit = {
                                onSpriteListEvent(SpriteListEvent.OpenDrawingScreen(meta?.spriteName, sprite, context))
                            },
                            onClick = {
                                onSpriteListEvent(SpriteListEvent.OpenPager(sprite))
                            },
                            sprite = sprite,
                            meta = meta,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }

            GalleryLayoutMode.StaggeredGrid -> {
                val staggeredState = rememberLazyStaggeredGridState()
                val isScrolled by remember { derivedStateOf { staggeredState.firstVisibleItemIndex > 0 } }
                LaunchedEffect(isScrolled) { onIsScrolledChange(isScrolled) }

                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    state = staggeredState,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp,
                    modifier = modifier
                ) {
                    items(
                        items = spriteList,
                        key = { it.sprite.id }
                    ) { spriteWithMeta ->
                        val (sprite, meta) = spriteWithMeta
                        SpriteGridCard(
                            spriteWithMeta = spriteWithMeta,
                            layoutMode = GalleryLayoutMode.StaggeredGrid,
                            onEdit = { onSpriteListEvent(SpriteListEvent.OpenDrawingScreen(meta?.spriteName, sprite, context)) },
                            onRename = { onSpriteListEvent(SpriteListEvent.OpenRenameDialog(sprite.id)) },
                            onDelete = { onSpriteListEvent(SpriteListEvent.OpenDeleteDialog(meta!!.spriteName, sprite.id)) },
                            onClick = { onSpriteListEvent(SpriteListEvent.OpenPager(sprite)) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }

            GalleryLayoutMode.SquareGrid -> {
                val gridState = rememberLazyGridState()
                val isScrolled by remember { derivedStateOf { gridState.firstVisibleItemIndex > 0 } }
                LaunchedEffect(isScrolled) { onIsScrolledChange(isScrolled) }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = modifier
                ) {
                    items(
                        items = spriteList,
                        key = { it.sprite.id }
                    ) { spriteWithMeta ->
                        val (sprite, meta) = spriteWithMeta
                        SpriteGridCard(
                            spriteWithMeta = spriteWithMeta,
                            layoutMode = GalleryLayoutMode.SquareGrid,
                            onEdit = { onSpriteListEvent(SpriteListEvent.OpenDrawingScreen(meta?.spriteName, sprite, context)) },
                            onRename = { onSpriteListEvent(SpriteListEvent.OpenRenameDialog(sprite.id)) },
                            onDelete = { onSpriteListEvent(SpriteListEvent.OpenDeleteDialog(meta!!.spriteName, sprite.id)) },
                            onClick = { onSpriteListEvent(SpriteListEvent.OpenPager(sprite)) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
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
                containerColor = AppTheme.colors.BackgroundColor,
            ),
            modifier = Modifier
                .padding(vertical = 8.dp)
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
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = meta?.spriteName ?: stringResource(R.string.untitled),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Box {
                    IconButton(
                        onClick = { showDropdown = true },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(40.dp)
                    ) {
                        PixelIcon(
                            icon =R.drawable.ic_three_dots,
                            contentDescription = stringResource(R.string.options),
                            tint = AppTheme.colors.TextColorLight
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

            AsyncCanvasPreviewer(
                sprite = sprite,
                meta = meta,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .align(Alignment.CenterHorizontally)
                    .clip(MaterialTheme.shapes.small),
                onClick = onClick
            )

            Spacer(modifier = Modifier.height(16.dp))
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
        containerColor = AppTheme.colors.DropDownMenuColor,
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.rename)) },
            trailingIcon = {
                PixelIcon(
                    icon = R.drawable.ic_info,
                    contentDescription = stringResource(R.string.rename),
                    tint = AppTheme.colors.TextColorLight,
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
                    tint = AppTheme.colors.TextColorLight
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
                    tint = AppTheme.colors.TextColorLight
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
            spriteList = DummyData.previewSprites,
            layoutMode = GalleryLayoutMode.SquareGrid
        )
    }
}