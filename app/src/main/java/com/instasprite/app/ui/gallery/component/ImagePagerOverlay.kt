package com.instasprite.app.ui.gallery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.instasprite.app.R
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.model.SpriteWithMeta
import com.instasprite.app.ui.components.composable.AsyncImageZoomableOverlay
import com.instasprite.app.ui.components.composable.IntColorPaletteView
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.composable.TopBar
import com.instasprite.app.ui.gallery.contract.ImagePagerEvent
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.drawCheckerboard
import com.instasprite.app.utils.pixelDp
import com.instasprite.app.utils.toDateString
import java.io.File

@Composable
fun ImagePagerOverlay(
    onImagePagerEvent: (ImagePagerEvent) -> Unit,
    spriteList: List<SpriteWithMeta>,
    startIndex: Int,
    onDismiss: (Sprite?) -> Unit,
    onOpenDrawing: (Sprite, String?) -> Unit,
    playingSpriteId: String? = null,
    playbackSprite: Sprite? = null
) {

    if (spriteList.isEmpty()) {
        return
    }

    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(spriteList.indices),
        pageCount = { spriteList.size })

    val currentSprite = spriteList.getOrNull(pagerState.currentPage)
    val spriteName = currentSprite?.meta?.spriteName ?: stringResource(R.string.untitled)
    val onEvent by rememberUpdatedState(onImagePagerEvent)
    DisposableEffect(currentSprite?.sprite?.id) {
        val id = currentSprite?.sprite?.id
        onDispose { if (id != null) onEvent(ImagePagerEvent.StopPlayback(id)) }
    }

    var zoomedPageIndex by remember { mutableStateOf<Int?>(null) }

    Dialog(
        onDismissRequest = {
            onDismiss(currentSprite?.sprite)
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Remove dialog dim
        (LocalView.current.parent as DialogWindowProvider).window.setDimAmount(0f)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                var dropdownMenuVisible by remember { mutableStateOf(false) }
                TopBar(
                    title = spriteName,
                    onBackClick = { onDismiss(currentSprite?.sprite) },
                    actions = {
                        if (currentSprite?.hasAnimation == true) {
                            Box(Modifier.padding(horizontal = 8.pixelDp)) {
                                SpritePlaybackButton(playingSpriteId == currentSprite.sprite.id) {
                                    onImagePagerEvent(ImagePagerEvent.TogglePlayback(currentSprite.sprite.id))
                                }
                            }
                        }
                        Box {
                            IconButton(
                                onClick = { dropdownMenuVisible = true }
                            ) {
                                PixelIcon(
                                    icon = R.drawable.ic_three_dots,
                                    contentDescription = stringResource(R.string.more),
                                    tint = AppTheme.colors.onSurface,
                                )
                            }

                            PagerDropdownMenu(
                                expanded = dropdownMenuVisible,
                                onDismiss = { dropdownMenuVisible = false },
                                onDeleteButtonTap = {
                                    currentSprite?.let {
                                        onImagePagerEvent(
                                            ImagePagerEvent.OpenDeleteDialog(
                                                spriteName,
                                                it.sprite.id
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomBar(
                    onEditButtonTap = {
                        currentSprite?.let {
                            onOpenDrawing(it.sprite, it.meta?.spriteName)
                        }
                    },
                    onExportTap = {
                        currentSprite?.let { onImagePagerEvent(ImagePagerEvent.OpenExportDialog(it)) }
                    },
                    spriteWithMetaData = currentSprite,
                    modifier = Modifier
                        .height(120.pixelDp)
                        .background(AppTheme.colors.background)
                )
            }
        ) { innerPadding ->

            HorizontalPager(
                state = pagerState,
                key = { index -> spriteList[index].sprite.id },
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.surfaceInset)
            ) { page ->
                val spriteWithMeta = spriteList[page]

                GallerySpritePreview(
                    playing = playingSpriteId == spriteWithMeta.sprite.id && page == pagerState.currentPage,
                    playbackSprite = playbackSprite?.takeIf { it.id == spriteWithMeta.sprite.id && page == pagerState.currentPage },
                    onStop = { onImagePagerEvent(ImagePagerEvent.StopPlayback(spriteWithMeta.sprite.id)) },
                    sprite = spriteWithMeta.sprite,
                    meta = spriteWithMeta.meta,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onClick = {
                        onImagePagerEvent(ImagePagerEvent.StopPlayback(spriteWithMeta.sprite.id))
                        zoomedPageIndex = page
                    }
                )

                if (zoomedPageIndex == page) {
                    AsyncImageZoomableOverlay(
                        model = File(
                            LocalContext.current.filesDir,
                            "thumbnail_${spriteWithMeta.sprite.id}.png"
                        ),
                        sufModifier = Modifier
                            .aspectRatio(spriteWithMeta.sprite.width.toFloat() / spriteWithMeta.sprite.height.toFloat())
                            .drawCheckerboard(
                                spriteWithMeta.sprite.width,
                                spriteWithMeta.sprite.height
                            ),
                        onDismiss = { zoomedPageIndex = null }
                    )
                }
            }
        }
    }
}

@Composable
internal fun BottomBar(
    spriteWithMetaData: SpriteWithMeta?,
    onExportTap: () -> Unit,
    onEditButtonTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (spriteWithMetaData == null) return

    val sprite = spriteWithMetaData.sprite
    val metadata = spriteWithMetaData.meta

    val spriteName = metadata?.spriteName ?: stringResource(R.string.untitled)

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.pixelDp)
        ) {
            Text(text = stringResource(R.string.name) + ": " + spriteName)
            metadata?.let {
                Text(text = stringResource(R.string.sort_date_created) + ": " + it.createdAt.toDateString())
            }

            Spacer(modifier = Modifier.height(8.pixelDp))

            if (sprite.colorPalette != null) {
                IntColorPaletteView(
                    colors = sprite.colorPalette,
                )
            }

            Spacer(modifier = Modifier.height(8.pixelDp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Button(
                    onClick = onExportTap,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.purple
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PixelIcon(
                            icon = R.drawable.ic_down_arrow,
                            contentDescription = stringResource(R.string.export_title),
                            tint = AppTheme.colors.onAccent,
                        )
                        Spacer(modifier = Modifier.width(6.pixelDp))
                        Text(
                            text = stringResource(R.string.export_title),
                            color = AppTheme.colors.onAccent
                        )
                    }
                }

                Button(
                    onClick = onEditButtonTap,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.green
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PixelIcon(
                            icon = R.drawable.ic_edit,
                            contentDescription = stringResource(R.string.edit_sprite),
                            tint = AppTheme.colors.onAccent,
                        )
                        Spacer(modifier = Modifier.width(6.pixelDp))
                        Text(
                            text = stringResource(R.string.edit),
                            color = AppTheme.colors.onAccent
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PagerDropdownMenu(
    expanded: Boolean = false,
    onDismiss: () -> Unit,
    onDeleteButtonTap: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surfaceContainer
    ) {
        DropdownMenuItem(
            text = {
                Text(text = stringResource(R.string.delete))
            },
            trailingIcon = {
                PixelIcon(
                    icon = R.drawable.ic_trash,
                    contentDescription = stringResource(R.string.delete_sprite),
                    tint = AppTheme.colors.onSurface,
                )
            },
            onClick = {
                onDeleteButtonTap()
                onDismiss()
            }
        )
    }
}
