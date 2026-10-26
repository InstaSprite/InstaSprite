package com.instasprite.app.ui.drawing.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Constraints
import com.instasprite.app.R
import com.instasprite.app.domain.model.Animation
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.ui.components.dialog.CustomDialog
import com.instasprite.app.ui.drawing.contract.LayerEvent
import com.instasprite.app.ui.drawing.contract.TimelineEvent
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.pixelDp

@Composable
fun AnimationTimeline(
    animation: Animation,
    layers: List<Layer>,
    activeFrameId: String,
    activeLayerId: String,
    isPlaying: Boolean,
    onEvent: (TimelineEvent) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    onLayerEvent: (LayerEvent) -> Unit = {},
    onionSkinEnabled: Boolean = false
) {
    var menu by remember { mutableStateOf(false) }
    var durationFrameId by remember { mutableStateOf<String?>(null) }
    val activeIndex = animation.frames.indexOfFirst { it.id == activeFrameId }.coerceAtLeast(0)
    val frame = animation.frames[activeIndex]
    val frameScroll = rememberLazyListState()
    val layerScroll = rememberLazyListState()
    val rows = remember(layers) { layers.asReversed() }
    val linkCounts =
        remember(animation.cels) { animation.cels.groupingBy { it.dataId }.eachCount() }
    val cells = remember(animation.cels) { animation.cels.associateBy { it.layerId to it.frameId } }
    val colors = AppTheme.colors

    LaunchedEffect(activeIndex) {
        if (frameScroll.layoutInfo.visibleItemsInfo.none { it.index == activeIndex }) {
            frameScroll.scrollToItem(activeIndex)
        }
    }

    LaunchedEffect(activeLayerId, rows.size) {
        val index = rows.indexOfFirst { it.id == activeLayerId }
        if (index >= 0 && layerScroll.layoutInfo.visibleItemsInfo.none { it.index == index }) {
            layerScroll.scrollToItem(index)
        }
    }

    Box(modifier) {
        Column(
            Modifier
                .fillMaxSize()
                .background(colors.BackgroundColorDarker)
        ) {
            if (!expanded) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(30.pixelDp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimelineToolbarButton(
                        if (isPlaying) R.drawable.ic_animation_stop else R.drawable.ic_arrow_head_right,
                        stringResource(if (isPlaying) R.string.animation_stop else R.string.animation_play),
                        { onEvent(TimelineEvent.TogglePlayback) },
                        enabled = animation.frames.size > 1,
                        active = isPlaying
                    )
                    TimelineFrameStrip(
                        animation, activeFrameId, activeLayerId, isPlaying,
                        onSelect = { onEvent(TimelineEvent.Select(it)) },
                        onDelete = { onEvent(TimelineEvent.Delete) },
                        onAdd = { onEvent(TimelineEvent.Append) }, modifier = Modifier.weight(1f)
                    )
                    TimelineToolbarButton(
                        R.drawable.ic_arrow_head_up,
                        stringResource(R.string.animation_expand), onClose
                    )
                }
            } else Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    Modifier
                        .width(76.pixelDp)
                        .fillMaxHeight()
                        .timelineGridBorder(colors.Foreground0Color, top = true, left = true)
                ) {
                    Row(
                        Modifier
                            .height(22.pixelDp)
                            .fillMaxWidth()
                            .background(colors.BackgroundColor)
                            .timelineGridBorder(colors.Foreground0Color),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimelineToolbarButton(
                            R.drawable.ic_left_arrow,
                            stringResource(R.string.animation_previous),
                            { onEvent(TimelineEvent.Select(animation.frames[activeIndex - 1].id)) },
                            enabled = !isPlaying && activeIndex > 0
                        )
                        TimelineToolbarButton(
                            if (isPlaying) R.drawable.ic_animation_stop else R.drawable.ic_arrow_head_right,
                            stringResource(if (isPlaying) R.string.animation_stop else R.string.animation_play),
                            { onEvent(TimelineEvent.TogglePlayback) },
                            enabled = animation.frames.size > 1,
                            active = isPlaying
                        )
                        TimelineToolbarButton(
                            R.drawable.ic_right_arrow,
                            stringResource(R.string.animation_next),
                            { onEvent(TimelineEvent.Select(animation.frames[activeIndex + 1].id)) },
                            enabled = !isPlaying && activeIndex < animation.frames.lastIndex
                        )
                    }
                    LazyColumn(state = layerScroll, modifier = Modifier.weight(1f)) {
                        items(rows, key = { it.id }) { layer ->
                            TimelineLayerRow(
                                layer, layer.id == activeLayerId,
                                onClick = {
                                    onEvent(
                                        TimelineEvent.Select(
                                            activeFrameId,
                                            layer.id
                                        )
                                    )
                                },
                                onVisibilityToggle = {
                                    onLayerEvent(
                                        LayerEvent.ToggleVisibility(
                                            layer.id
                                        )
                                    )
                                },
                                onLockToggle = { onLayerEvent(LayerEvent.ToggleLock(layer.id)) })
                        }
                    }
                }
                LazyRow(
                    state = frameScroll, modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    itemsIndexed(animation.frames, key = { _, value -> value.id }) { index, value ->
                        var deleteMenu by remember { mutableStateOf(false) }
                        val current = value.id == activeFrameId
                        Column(
                            Modifier
                                .width(22.pixelDp)
                                .fillMaxHeight()
                                .timelineGridBorder(
                                    colors.Foreground0Color,
                                    topColor = if (current) colors.SelectedColor else colors.Foreground0Color,
                                    top = true
                                )
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(22.pixelDp)
                                    .background(colors.BackgroundColor)
                                    .timelineGridBorder(colors.Foreground0Color)
                                    .combinedClickable(
                                        onClick = { onEvent(TimelineEvent.Select(value.id)) },
                                        onLongClick = {
                                            onEvent(TimelineEvent.Select(value.id))
                                            deleteMenu = true
                                        }),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    (index + 1).toString(),
                                    color = colors.TextColorLight,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                TimelineFrameDeleteMenu(
                                    deleteMenu, current && animation.frames.size > 1,
                                    onDismiss = { deleteMenu = false },
                                    onDelete = { onEvent(TimelineEvent.Delete) }
                                )
                            }
                            SubcomposeLayout(
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clipToBounds()
                                    .scrollable(
                                        layerScroll,
                                        Orientation.Vertical,
                                        reverseDirection = true
                                    )
                            ) { constraints ->
                                val rowPlaceables =
                                    layerScroll.layoutInfo.visibleItemsInfo.mapNotNull { row ->
                                        val layer =
                                            rows.getOrNull(row.index) ?: return@mapNotNull null
                                        val placeable = subcompose(row.key) {
                                            val cel = cells[layer.id to value.id]
                                            val linked =
                                                cel != null && (linkCounts[cel.dataId] ?: 0) > 1
                                            val selected = current && layer.id == activeLayerId
                                            val description = stringResource(
                                                R.string.animation_cell, layer.name, index + 1,
                                                stringResource(
                                                    if (cel == null) R.string.animation_cell_empty else if (linked)
                                                        R.string.animation_cell_linked else R.string.animation_cell_filled
                                                )
                                            )
                                            val previous = animation.frames.getOrNull(index - 1)
                                                ?.let { cells[layer.id to it.id] }
                                            val next = animation.frames.getOrNull(index + 1)
                                                ?.let { cells[layer.id to it.id] }
                                            TimelineCelCell(
                                                hasCel = cel != null,
                                                isLinked = linked,
                                                linkLeft = cel != null && previous?.dataId == cel.dataId,
                                                linkRight = cel != null && next?.dataId == cel.dataId,
                                                isSelected = selected,
                                                description = description,
                                                onLongClick = {
                                                    onEvent(
                                                        TimelineEvent.Select(
                                                            value.id,
                                                            layer.id
                                                        )
                                                    )
                                                    menu = true
                                                },
                                                onClick = {
                                                    onEvent(
                                                        TimelineEvent.Select(
                                                            value.id,
                                                            layer.id
                                                        )
                                                    )
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            )
                                        }.single().measure(
                                            Constraints.fixed(
                                                constraints.maxWidth,
                                                row.size
                                            )
                                        )
                                        placeable to row.offset
                                    }
                                layout(constraints.maxWidth, constraints.maxHeight) {
                                    rowPlaceables.forEach { (placeable, offset) ->
                                        placeable.placeRelative(0, offset)
                                    }
                                }
                            }
                        }
                    }
                    item(key = "add-frame") {
                        TimelineToolbarButton(
                            R.drawable.ic_plus, stringResource(R.string.animation_add),
                            { onEvent(TimelineEvent.Append) },
                            enabled = !isPlaying && animation.frames.size < Animation.MAX_FRAMES
                        )
                    }
                }
            }
            if (expanded) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(2.pixelDp)
                ) {
                    Box(Modifier.width(76.pixelDp))
                    TimelineScrollbar(frameScroll, 22.pixelDp, Modifier.weight(1f))
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(22.pixelDp)
                        .background(colors.BackgroundColorDarker)
                        .padding(horizontal = 3.pixelDp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.pixelDp)
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(22.pixelDp)
                            .clickable(enabled = !isPlaying) { durationFrameId = activeFrameId },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.pixelDp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                layers.firstOrNull { it.id == activeLayerId }?.name.orEmpty(),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                                color = colors.TextColorLight
                            )
                            Text(
                                stringResource(
                                    R.string.animation_position,
                                    activeIndex + 1,
                                    animation.frames.size
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                color = colors.TextColorLight
                            )
                            Text(
                                stringResource(R.string.animation_frame_time, frame.durationMs),
                                style = MaterialTheme.typography.labelSmall, maxLines = 1
                            )
                        }
                    }
                    Box {
                        TimelineToolbarButton(
                            R.drawable.ic_menu,
                            stringResource(R.string.animation_actions),
                            { menu = true },
                            enabled = !isPlaying
                        )
                        DropdownMenu(
                            expanded = menu, onDismissRequest = { menu = false },
                            modifier = Modifier.background(colors.BackgroundColor)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(if (onionSkinEnabled) R.string.onion_skin_on else R.string.onion_skin_off)) },
                                onClick = { onEvent(TimelineEvent.ToggleOnionSkin) })
                            val actions = listOf(
                                R.string.animation_duplicate to TimelineEvent.Insert(),
                                R.string.animation_empty to TimelineEvent.Insert(empty = true),
                                R.string.animation_linked to TimelineEvent.Insert(linked = true),
                                R.string.animation_delete to TimelineEvent.Delete,
                                R.string.animation_copy_cel to TimelineEvent.CopyPrevious,
                                R.string.animation_link_cel to TimelineEvent.LinkPrevious,
                                R.string.animation_unlink to TimelineEvent.Unlink,
                                R.string.animation_clear to TimelineEvent.ClearCel,
                                R.string.animation_poster to TimelineEvent.SetPoster
                            )
                            actions.forEach { (label, event) ->
                                val enabled = when (event) {
                                    TimelineEvent.Delete -> animation.frames.size > 1
                                    is TimelineEvent.Insert -> animation.frames.size < Animation.MAX_FRAMES
                                    TimelineEvent.CopyPrevious, TimelineEvent.LinkPrevious -> activeIndex > 0
                                    else -> true
                                }
                                DropdownMenuItem(
                                    text = { Text(stringResource(label)) }, enabled = enabled,
                                    onClick = { menu = false; onEvent(event) })
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.animation_duration)) },
                                onClick = { menu = false; durationFrameId = activeFrameId })
                        }
                    }
                    TimelineToolbarButton(
                        R.drawable.ic_arrow_head_down,
                        stringResource(R.string.animation_collapse),
                        onClose
                    )
                }
            }
        }
    }
    durationFrameId?.let { targetId ->
        val target = animation.frames.firstOrNull { it.id == targetId } ?: return@let
        var input by remember(target.id) { mutableStateOf(target.durationMs.toString()) }
        val value = input.toIntOrNull()
        CustomDialog(
            title = stringResource(R.string.animation_duration),
            onDismiss = { durationFrameId = null },
            onConfirm = {
                if (value != null && value in 1..60_000) {
                    onEvent(TimelineEvent.Select(target.id))
                    onEvent(TimelineEvent.Duration(value))
                    durationFrameId = null
                }
            }) {
            OutlinedTextField(
                value = input, onValueChange = { input = it.filter(Char::isDigit).take(5) },
                label = { Text(stringResource(R.string.animation_milliseconds)) },
                isError = value == null || value !in 1..60_000,
                supportingText = { Text(stringResource(R.string.animation_duration_range)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = colors.outlineTextFieldColors(), singleLine = true
            )
        }
    }
}

@Preview(name = "Timeline transport states", widthDp = 400, heightDp = 180)
@Preview(name = "Narrow timeline", widthDp = 320, heightDp = 180)
@Composable
private fun AnimationTimelinePreview(@PreviewParameter(TimelinePreviewProvider::class) playing: Boolean) {
    InstaSpriteTheme {
        AnimationTimeline(
            TimelinePreviewData.animation, TimelinePreviewData.layers,
            "frame-2", "character", playing, {}, {}, Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Empty document", widthDp = 360, heightDp = 128)
@Composable
private fun EmptyAnimationTimelinePreview() {
    val animation = remember { Animation() }
    InstaSpriteTheme {
        AnimationTimeline(
            animation,
            TimelinePreviewData.layers.take(1),
            animation.frames.first().id,
            "background",
            false,
            {},
            {},
            Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Collapsed timeline", widthDp = 360, heightDp = 45)
@Preview(name = "Narrow collapsed timeline", widthDp = 320, heightDp = 45)
@Composable
private fun CollapsedAnimationTimelinePreview() {
    InstaSpriteTheme {
        AnimationTimeline(
            TimelinePreviewData.animation, TimelinePreviewData.layers,
            "frame-2", "character", false, {}, {}, Modifier.fillMaxSize(), expanded = false
        )
    }
}
