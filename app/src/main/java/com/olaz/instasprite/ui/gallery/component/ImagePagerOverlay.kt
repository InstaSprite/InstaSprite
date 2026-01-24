package com.olaz.instasprite.ui.gallery.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.domain.model.SpriteWithMeta
import com.olaz.instasprite.domain.export.ImageExporter
import com.olaz.instasprite.ui.components.composable.ImageZoomableOverlay
import com.olaz.instasprite.ui.gallery.contract.ImagePagerEvent
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.utils.toDateString

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

    val sprites = spriteList.map { it.sprite }
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
                TopBar(
                    onDismiss = { onDismiss(currentSprite?.sprite) },
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
            },
            bottomBar = {
                BottomBar(
                    onEditButtonTap = {
                        currentSprite?.let {
                            onImagePagerEvent(
                                ImagePagerEvent.OpenDrawingActivity(
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
                        .height(180.dp)
                        .background(CatppuccinUI.BackgroundColor)
                )
            }
        ) { innerPadding ->

            HorizontalPager(
                state = pagerState,
                key = { index -> spriteList[index].sprite.id },
                modifier = Modifier
                    .fillMaxSize()
                    .background(CatppuccinUI.BackgroundColorDarker)
            ) { page ->
                val sprite = sprites[page]
                val bitmapImage = remember(sprite) {
                    ImageExporter.convertToBitmap(
                        sprite.pixelsData.map { Color(it) },
                        sprite.width,
                        sprite.height,
                    )?.asImageBitmap()
                }

                if (bitmapImage != null) {
                    Image(
                        bitmap = bitmapImage,
                        contentDescription = "Zoomed Sprite $page",
                        contentScale = ContentScale.Fit,
                        filterQuality = FilterQuality.None,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { zoomedPageIndex = page }
                            )
                    )

                    if (zoomedPageIndex == page) {
                        ImageZoomableOverlay(
                            bitmap = bitmapImage,
                            onDismiss = { zoomedPageIndex = null }
                        )
                    }
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
                .padding(12.dp)
        ) {
            Text(text = "Name: $spriteName")

            Text(text = "Date created: $dateCreated")

            Text(text = "Last modified: $dateModified")

            Text(text = "Dimensions: $spriteWidth x $spriteHeight")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Button(
                    onClick = onSaveImageTap,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CatppuccinUI.SelectedColor
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Save as image",
                            tint = CatppuccinUI.TextColorDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Save as Image",
                            color = CatppuccinUI.TextColorDark
                        )
                    }
                }

                Button(
                    onClick = onEditButtonTap,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CatppuccinUI.AccentButtonColor
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit sprite",
                            tint = CatppuccinUI.TextColorDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Edit",
                            color = CatppuccinUI.TextColorDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    onDismiss: () -> Unit,
    onDeleteButtonTap: () -> Unit
) {
    var dropdownMenuVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(CatppuccinUI.BackgroundColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .align(Alignment.CenterStart)
        ) {
            IconButton(
                onClick = onDismiss
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Dismiss",
                    tint = CatppuccinUI.DismissButtonColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Box {
                IconButton(
                    onClick = { dropdownMenuVisible = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = CatppuccinUI.TextColorLight,
                        modifier = Modifier.size(32.dp)
                    )
                }

                PagerDropdownMenu(
                    expanded = dropdownMenuVisible,
                    onDismiss = { dropdownMenuVisible = false },
                    onDeleteButtonTap = {
                        onDeleteButtonTap()
                    }
                )
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
        containerColor = CatppuccinUI.DropDownMenuColor
    ) {
        DropdownMenuItem(
            text = {
                Text(text = "Delete")
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete sprite",
                    tint = CatppuccinUI.TextColorLight
                )
            },
            onClick = {
                onDeleteButtonTap()
                onDismiss()
            }
        )
    }
}