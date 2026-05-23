package com.instasprite.app.ui.gallery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.domain.model.Cel
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.model.SpriteMeta
import com.instasprite.app.domain.model.SpriteWithMeta
import com.instasprite.app.ui.components.composable.AsyncCanvasPreviewer
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.gallery.GalleryLayoutMode
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme

@Composable
fun SpriteGridCard(
    spriteWithMeta: SpriteWithMeta,
    layoutMode: GalleryLayoutMode,
    onEdit: () -> Unit = {},
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {},
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sprite = spriteWithMeta.sprite
    val meta = spriteWithMeta.meta
    var showDropdown by remember { mutableStateOf(false) }

    val cardAspectRatio = if (layoutMode == GalleryLayoutMode.SquareGrid) {
        1f
    } else {
        sprite.width.toFloat() / sprite.height.toFloat()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(AppTheme.colors.BackgroundColor)
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
                onLongClick = { showDropdown = true }
            )
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.BackgroundColor.copy(0.9f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = meta?.spriteName ?: stringResource(R.string.untitled),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
                Box {
                    IconButton(
                        onClick = { showDropdown = true },
                        modifier = Modifier.size(36.dp),
                        ) {
                        PixelIcon(
                            icon = R.drawable.ic_three_dots,
                            contentDescription = stringResource(R.string.options),
                            tint = AppTheme.colors.TextColorLight
                        )
                    }
                    SpriteDropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        onEdit = { onEdit(); showDropdown = false },
                        onRename = { onRename(); showDropdown = false },
                        onDelete = { onDelete(); showDropdown = false }
                    )
                }
            }

            AsyncCanvasPreviewer(
                sprite = sprite,
                meta = meta,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(cardAspectRatio),
                onClick = onClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SpriteGridCardStaggeredPreview() {
    InstaSpriteTheme {
        SpriteGridCard(
            spriteWithMeta = SpriteWithMeta(
                sprite = Sprite(
                    id = "1",
                    width = 16,
                    height = 24,
                    layers = listOf(
                        Layer(
                            id = "l1", name = "Layer 1", cel = Cel(
                                x = 0,
                                y = 0,
                                width = 16,
                                height = 24,
                                pixels = IntArray(16 * 24) { AppTheme.colors.SelectedColor.hashCode() })))),
                meta = SpriteMeta(spriteId = "1", spriteName = "My Sprite")),
            layoutMode = GalleryLayoutMode.StaggeredGrid)
    }
}

@Preview(showBackground = true)
@Composable
private fun SpriteGridCardSquarePreview() {
    InstaSpriteTheme {
        SpriteGridCard(
            spriteWithMeta = SpriteWithMeta(
                sprite = Sprite(
                    id = "2",
                    width = 32,
                    height = 32,
                    layers = listOf(
                        Layer(
                            id = "l2", name = "Layer 1", cel = Cel(
                                x = 0,
                                y = 0,
                                width = 32,
                                height = 32,
                                pixels = IntArray(32 * 32) { AppTheme.colors.AccentButtonColor.hashCode() })))),
                meta = SpriteMeta(
                    spriteId = "2",
                    spriteName = "Long ahh name asdkjasdlkajsdlkasjdaslkdjad askldjaslkdjasdlkasjdalkdjas"
                )), layoutMode = GalleryLayoutMode.SquareGrid)
    }
}
