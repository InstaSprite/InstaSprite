package com.instasprite.app.ui.drawing.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.domain.model.Animation
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.ui.components.dialog.CustomDialog
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.pixelDp

@Composable
fun AnimationTimeline(
    animation: Animation,
    layers: List<Layer>,
    activeFrameId: String,
    activeLayerId: String,
    isPlaying: Boolean,
    onEvent: (TimelineEvent) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    var menu by remember { mutableStateOf(false) }
    var durationDialog by remember { mutableStateOf(false) }
    val activeIndex = animation.frames.indexOfFirst { it.id == activeFrameId }.coerceAtLeast(0)
    val frame = animation.frames[activeIndex]
    val frameScroll = rememberLazyListState()
    val layerScroll = rememberLazyListState()
    val density = LocalDensity.current
    val rowViewportHeight = (layers.size.coerceIn(1, 3) * 32).pixelDp
    val rows = remember(layers) { layers.asReversed() }
    val linkCounts = remember(animation.cels) { animation.cels.groupingBy { it.dataId }.eachCount() }
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

    Column(Modifier.fillMaxWidth().background(colors.BackgroundColorDarker)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(stringResource(if (expanded) R.string.timeline_collapse else R.string.timeline_expand))
            }
            TextButton(onClick = { onEvent(TimelineEvent.TogglePlayback) }, enabled = animation.frames.size > 1) {
                Text(stringResource(if (isPlaying) R.string.animation_stop else R.string.animation_play))
            }
            Text(
                stringResource(R.string.animation_position, activeIndex + 1, animation.frames.size),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f),
                color = colors.TextColorLight
            )
            Box {
                TextButton(onClick = { menu = true }, enabled = !isPlaying) { Text(stringResource(R.string.animation_actions)) }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false },
                    modifier = Modifier.background(colors.BackgroundColor)) {
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
                        DropdownMenuItem(text = { Text(stringResource(label)) }, enabled = enabled,
                            onClick = { menu = false; onEvent(event) })
                    }
                    DropdownMenuItem(text = { Text(stringResource(R.string.animation_duration)) },
                        onClick = { menu = false; durationDialog = true })
                }
            }
        }
        if (expanded) {
            Row {
                Column(Modifier.width(76.pixelDp)) {
                    Box(Modifier.height(32.pixelDp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.layers), style = MaterialTheme.typography.labelSmall)
                    }
                    LazyColumn(state = layerScroll, modifier = Modifier.height(rowViewportHeight)) {
                        items(rows, key = { it.id }) { layer ->
                            Box(Modifier.height(32.pixelDp).fillMaxWidth()
                                .background(if (layer.id == activeLayerId) colors.BackgroundColor else colors.BackgroundColorDarker)
                                .clickable { onEvent(TimelineEvent.Select(activeFrameId, layer.id)) }
                                .padding(horizontal = 4.pixelDp), contentAlignment = Alignment.CenterStart) {
                                Text(layer.name, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelSmall, color = colors.TextColorLight)
                            }
                        }
                    }
                }
                LazyRow(state = frameScroll, modifier = Modifier.weight(1f)) {
                    itemsIndexed(animation.frames, key = { _, value -> value.id }) { index, value ->
                        Column(Modifier.width(38.pixelDp)) {
                            val current = value.id == activeFrameId
                            Box(Modifier.fillMaxWidth().height(32.pixelDp)
                                .background(if (current) colors.SelectedColor else colors.BackgroundColor)
                                .clickable { onEvent(TimelineEvent.Select(value.id)) }, contentAlignment = Alignment.Center) {
                                Text((index + 1).toString(), color = if (current) colors.TextColorDark else colors.TextColorLight,
                                    style = MaterialTheme.typography.labelMedium)
                            }
                            Box(Modifier.fillMaxWidth().height(rowViewportHeight).clipToBounds()
                                .scrollable(layerScroll, Orientation.Vertical, reverseDirection = true)) {
                                layerScroll.layoutInfo.visibleItemsInfo.forEach { row ->
                                val layer = rows.getOrNull(row.index) ?: return@forEach
                                val cel = cells[layer.id to value.id]
                                val linked = cel != null && (linkCounts[cel.dataId] ?: 0) > 1
                                val selected = current && layer.id == activeLayerId
                                val description = stringResource(R.string.animation_cell, layer.name, index + 1,
                                    stringResource(if (cel == null) R.string.animation_cell_empty else if (linked)
                                        R.string.animation_cell_linked else R.string.animation_cell_filled))
                                Box(Modifier.offset(y = with(density) { row.offset.toDp() }).fillMaxWidth().height(32.pixelDp)
                                    .background(if (current) colors.BackgroundColor else colors.BackgroundColorDarker)
                                    .border(if (selected) 2.dp else 0.5.dp, if (selected) colors.SelectedColor else colors.Foreground2Color)
                                    .semantics { contentDescription = description }
                                    .clickable { onEvent(TimelineEvent.Select(value.id, layer.id)) },
                                    contentAlignment = Alignment.Center) {
                                    Canvas(Modifier.size(14.pixelDp)) {
                                        val tint = if (selected) colors.SelectedColor else colors.TextColorLight
                                        if (cel == null) drawCircle(tint.copy(alpha = 0.35f), radius = size.minDimension / 4,
                                            style = Stroke(1.dp.toPx()))
                                        else if (linked) {
                                            drawCircle(tint, size.minDimension / 4, Offset(size.width * 0.35f, center.y), style = Stroke(2.dp.toPx()))
                                            drawCircle(tint, size.minDimension / 4, Offset(size.width * 0.65f, center.y), style = Stroke(2.dp.toPx()))
                                        } else drawRect(tint, topLeft = Offset(size.width / 4, size.height / 4),
                                            size = size / 2f)
                                    }
                                }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (durationDialog) {
        var input by remember(frame.id) { mutableStateOf(frame.durationMs.toString()) }
        val value = input.toIntOrNull()
        CustomDialog(title = stringResource(R.string.animation_duration), onDismiss = { durationDialog = false },
            onConfirm = {
                if (value != null && value in 1..60_000) {
                    onEvent(TimelineEvent.Duration(value))
                    durationDialog = false
                }
            }) {
            OutlinedTextField(value = input, onValueChange = { input = it.filter(Char::isDigit).take(5) },
                label = { Text(stringResource(R.string.animation_milliseconds)) },
                isError = value == null || value !in 1..60_000,
                supportingText = { Text(stringResource(R.string.animation_duration_range)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = colors.outlineTextFieldColors(), singleLine = true)
        }
    }
}
