package com.olaz.instasprite.ui.gallery.component

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.R
import com.olaz.instasprite.domain.model.Sprite
import com.olaz.instasprite.domain.model.SpriteMeta
import com.olaz.instasprite.domain.model.SpriteWithMeta
import com.olaz.instasprite.ui.components.composable.AsyncCanvasPreviewer
import com.olaz.instasprite.ui.gallery.contract.SpriteListEvent
import com.olaz.instasprite.ui.theme.CatppuccinUI


@Composable
fun SpriteList(
    onSpriteListEvent: (SpriteListEvent) -> Unit,
    spriteList: List<SpriteWithMeta>,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val context = LocalContext.current

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
                    onSpriteListEvent(
                        SpriteListEvent.OpenDeleteDialog(
                            meta!!.spriteName,
                            sprite.id
                        )
                    )
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
                containerColor = CatppuccinUI.BackgroundColor,
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
                    text = meta?.spriteName ?: "Untitled",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Box {
                    IconButton(
                        onClick = { showDropdown = true },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_three_dots),
                            contentDescription = "Options",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
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
                    .clip(RoundedCornerShape(12.dp)),
                onClick = onClick
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SpriteDropdownMenu(
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
        containerColor = CatppuccinUI.DropDownMenuColor,
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = { Text("Rename") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Rename",
                    tint = CatppuccinUI.TextColorLight
                )
            },
            onClick = onRename,
        )
        DropdownMenuItem(
            text = { Text("Edit") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = CatppuccinUI.TextColorLight
                )
            },
            onClick = onEdit,
        )
        DropdownMenuItem(
            text = { Text("Delete") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = CatppuccinUI.TextColorLight
                )
            },
            onClick = onDelete,
        )
    }
}