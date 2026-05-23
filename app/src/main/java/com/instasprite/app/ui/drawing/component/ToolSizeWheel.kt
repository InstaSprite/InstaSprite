package com.instasprite.app.ui.drawing.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme

@Composable
fun ToolSizeWheelPopup(
    toolSize: Int,
    itemHeight: Dp,
    onDismiss: () -> Unit,
    onValueChange: (Int) -> Unit
) {
    val visibleItems = 5
    val wheelHeight = itemHeight * visibleItems

    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .width(70.dp)
                .height(wheelHeight),
            color = AppTheme.colors.BackgroundColor,
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 12.dp
        ) {
            ToolSizeWheelPicker(
                toolSizeValue = toolSize,
                onValueChange = onValueChange,
                itemHeight = itemHeight,
                modifier = Modifier.fillMaxSize(),
                onItemClick = {
                    onValueChange(it)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun ToolSizeWheelPicker(
    toolSizeValue: Int,
    onValueChange: (Int) -> Unit,
    itemHeight: Dp,
    modifier: Modifier = Modifier,
    onItemClick: (value: Int) -> Unit = {}
) {
    val range = 1..10

    val visibleItems = 5
    val wheelHeight = itemHeight * visibleItems

    val listState = rememberLazyListState()
    val fling = rememberSnapFlingBehavior(listState)

    LaunchedEffect(Unit) {
        listState.scrollToItem(toolSizeValue - range.first)
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling ->
                if (!scrolling) {
                    val index = listState.firstVisibleItemIndex
                        .coerceIn(0, range.count() - 1)

                    onValueChange(range.first + index)
                }
            }
    }

    Box(
        modifier = modifier
            .width(70.dp)
            .height(wheelHeight)
    ) {

        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .background(AppTheme.colors.BackgroundColorDarker)
        ) {
            PixelIcon(
                icon = R.drawable.ic_arrow_head_right,
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterStart),
                tint = AppTheme.colors.AccentButtonColor
            )
        }

        LazyColumn(
            state = listState,
            flingBehavior = fling,
            reverseLayout = true,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeight * 2),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(range.count()) { i ->
                val value = range.first + i

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .clickable { onItemClick(value) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$value px",
                        fontSize = 12.sp,
                        color = AppTheme.colors.TextColorLight
                    )
                }
            }
        }
    }
}



@Preview
@Composable
private fun PreviewWheel() {
    InstaSpriteTheme() {
        ToolSizeWheelPopup(
            toolSize = 1,
            itemHeight = 36.dp,
            onDismiss = {},
            onValueChange = {}
        )
    }
}