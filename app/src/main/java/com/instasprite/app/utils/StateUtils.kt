package com.instasprite.app.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.State

@Composable
fun rememberBottomBarVisibleState(
    lazyListState: LazyListState
): State<Boolean> {
    val visible = remember { mutableStateOf(true) }
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.firstVisibleItemIndex to lazyListState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            val scrollingUp = index < previousIndex ||
                    (index == previousIndex && offset < previousOffset)

            // Force show at top
            visible.value = index == 0 && offset < 10 || scrollingUp

            previousIndex = index
            previousOffset = offset
        }
    }

    return visible
}
