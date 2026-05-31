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
import com.instasprite.app.ui.components.composable.AsyncCanvasPreviewer
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
    onDismiss: (Sprite?) -> Unit
) {
    val context = LocalContext.current

    if (spriteList.isEmpty()) {
        return
    }

    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { spriteList.size })

    val currentSprite = spriteList.getOrNull(pagerState.currentPage)

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
                    title = currentSprite?.meta?.spriteName ?: "",
                    onBackClick = { onDismiss(currentSprite?.sprite) },
                    actions = {
                        Box {
                            IconButton(
                                onClick = { dropdownMenuVisible = true }
                            ) {
                                PixelIcon(
                                    icon = R.drawable.ic_three_dots,
                                    contentDescription = stringResource(R.string.more),
                                    tint = AppTheme.colors.TextColorLight,
                                )
                            }

                            PagerDropdownMenu(
                                expanded = dropdownMenuVisible,
                                onDismiss = { dropdownMenuVisible = false },
                                onDeleteButtonTap = {
                                    currentSprite?.meta?.let {
                                        onImagePagerEvent(
                                            ImagePagerEvent.OpenDeleteDialog(
                                                it.spriteName,
                                                it.spriteId
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
                            onImagePagerEvent(
                                ImagePagerEvent.OpenDrawingActivity(
                                    it.meta?.spriteName,
                                    it.sprite,
                                    context
                                )
                            )
                        }
                    },
                    onSaveImageTap = {
                        onImagePagerEvent(ImagePagerEvent.OpenSaveImageDialog(currentSprite!!))
                    },
                    spriteWithMetaData = currentSprite,
                    modifier = Modifier
                        .height(120.pixelDp)
                        .background(AppTheme.colors.BackgroundColor)
                )
            }
        ) { innerPadding ->

            HorizontalPager(
                state = pagerState,
                key = { index -> spriteList[index].sprite.id },
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.BackgroundColorDarker)
            ) { page ->
                val spriteWithMeta = spriteList[page]

                AsyncCanvasPreviewer(
                    sprite = spriteWithMeta.sprite,
                    meta = spriteWithMeta.meta,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onClick = { zoomedPageIndex = page }
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
private fun BottomBar(
    spriteWithMetaData: SpriteWithMeta?,
    onSaveImageTap: () -> Unit,
    onEditButtonTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (spriteWithMetaData == null) return

    val sprite = spriteWithMetaData.sprite
    val metadata = spriteWithMetaData.meta!!

    val spriteName = metadata.spriteName
    val dateCreated = metadata.createdAt.toDateString()
    val dateModified = metadata.lastModifiedAt.toDateString()

    val spriteWidth = sprite.width
    val spriteHeight = sprite.height

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.pixelDp)
        ) {
            Text(text =  stringResource(R.string.name) + ": " + spriteName)
            Text(text = stringResource(R.string.sort_date_created) + ": " + dateCreated)

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
                    onClick = onSaveImageTap,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.SelectedColor
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PixelIcon(
                            icon = R.drawable.ic_down_arrow,
                            contentDescription = stringResource(R.string.save_as_image_1),
                            tint = AppTheme.colors.TextColorDark,
                        )
                        Spacer(modifier = Modifier.width(6.pixelDp))
                        Text(
                            text = stringResource(R.string.save_as_image),
                            color = AppTheme.colors.TextColorDark
                        )
                    }
                }

                Button(
                    onClick = onEditButtonTap,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.AccentButtonColor
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PixelIcon(
                            icon = R.drawable.ic_edit,
                            contentDescription = stringResource(R.string.edit_sprite),
                            tint = AppTheme.colors.TextColorDark,
                        )
                        Spacer(modifier = Modifier.width(6.pixelDp))
                        Text(
                            text = stringResource(R.string.edit),
                            color = AppTheme.colors.TextColorDark
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
        containerColor = AppTheme.colors.DropDownMenuColor
    ) {
        DropdownMenuItem(
            text = {
                Text(text = stringResource(R.string.delete))
            },
            trailingIcon = {
                PixelIcon(
                    icon = R.drawable.ic_trash,
                    contentDescription = stringResource(R.string.delete_sprite),
                    tint = AppTheme.colors.TextColorLight,
                )
            },
            onClick = {
                onDeleteButtonTap()
                onDismiss()
            }
        )
    }
}