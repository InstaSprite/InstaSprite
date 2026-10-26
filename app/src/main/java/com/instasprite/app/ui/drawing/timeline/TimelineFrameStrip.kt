package com.instasprite.app.ui.drawing.timeline

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.instasprite.app.R
import com.instasprite.app.domain.model.Animation
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.pixelDp

@Composable
internal fun TimelineFrameStrip(
    animation: Animation,
    activeFrameId: String,
    activeLayerId: String,
    isPlaying: Boolean,
    onSelect: (String) -> Unit,
    onDelete: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberLazyListState()
    val index = animation.frames.indexOfFirst { it.id == activeFrameId }.coerceAtLeast(0)
    val colors = AppTheme.colors

    LaunchedEffect(index) {
        if (index == animation.frames.lastIndex || scroll.layoutInfo.visibleItemsInfo.none { it.index == index }) {
            scroll.scrollToItem(index)
        }
    }

    val layerCels = remember(animation.cels, activeLayerId) {
        animation.cels.filter { it.layerId == activeLayerId }.associateBy { it.frameId }
    }
    val linkCounts =
        remember(animation.cels) { animation.cels.groupingBy { it.dataId }.eachCount() }
    Column(
        modifier
            .fillMaxWidth()
            .height(30.pixelDp)
    ) {
        LazyRow(
            state = scroll, modifier = Modifier
                .fillMaxWidth()
                .height(28.pixelDp)
        ) {
            itemsIndexed(animation.frames, key = { _, frame -> frame.id }) { frameIndex, frame ->
                var deleteMenu by remember { mutableStateOf(false) }
                val active = frameIndex == index
                val cel = layerCels[frame.id]
                Column(
                    Modifier
                        .width(28.pixelDp)
                        .height(28.pixelDp)
                        .semantics { selected = active }
                        .drawBehind {
                            if (active) drawLine(
                                colors.LinkColor,
                                Offset(3.pixelDp.toPx(), 1.pixelDp.toPx()),
                                Offset(size.width - 3.pixelDp.toPx(), 1.pixelDp.toPx()),
                                2.pixelDp.toPx()
                            )
                        }
                        .combinedClickable(
                            onClick = { onSelect(frame.id) },
                            onLongClick = { onSelect(frame.id); deleteMenu = true }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        (frameIndex + 1).toString(), style = MaterialTheme.typography.labelSmall,
                        color = colors.TextColorLight
                    )
                    val celDescription = stringResource(
                        when {
                            cel == null -> R.string.animation_cell_empty
                            (linkCounts[cel.dataId] ?: 0) > 1 -> R.string.animation_cell_linked
                            else -> R.string.animation_cell_filled
                        }
                    )
                    Box(
                        Modifier
                            .size(16.pixelDp)
                            .semantics { contentDescription = celDescription },
                        contentAlignment = Alignment.Center
                    ) {
                        if (cel != null) PixelIcon(
                            R.drawable.ic_small_circle_shape,
                            tint = colors.TextColorLight
                        )
                    }
                    TimelineFrameDeleteMenu(
                        deleteMenu, active && animation.frames.size > 1,
                        onDismiss = { deleteMenu = false }, onDelete = onDelete
                    )
                }
            }
            item(key = "add-frame") {
                Box(Modifier.size(28.pixelDp), contentAlignment = Alignment.Center) {
                    TimelineToolbarButton(
                        R.drawable.ic_plus, stringResource(R.string.animation_add),
                        onAdd, enabled = !isPlaying && animation.frames.size < Animation.MAX_FRAMES
                    )
                }
            }
        }
        TimelineScrollbar(scroll, 28.pixelDp)
    }
}

@Preview(name = "Collapsed frame strip", widthDp = 360)
@Composable
private fun TimelineFrameStripPreview() {
    InstaSpriteTheme {
        TimelineFrameStrip(TimelinePreviewData.animation, "frame-2", "character", false, {}, {}, {})
    }
}
