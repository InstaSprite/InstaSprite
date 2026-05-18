package com.instasprite.app.ui.components.composable

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instasprite.app.ui.theme.AppTheme

enum class DrawerSide {
    Start,
    End
}

@Composable
fun DrawerLayout(
    isOpen: Boolean,
    onDrawerClose: () -> Unit,
    side: DrawerSide = DrawerSide.End,
    width: Dp = 300.dp,
    drawerContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    BackHandler(enabled = isOpen) {
        onDrawerClose()
    }

    val alignment =
        if (side == DrawerSide.Start)
            Alignment.CenterStart
        else
            Alignment.CenterEnd

    val enter = if (side == DrawerSide.Start) {
        slideInHorizontally(initialOffsetX = { -it })
    } else {
        slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth })
    }

    val exit = if (side == DrawerSide.Start) {
        slideOutHorizontally(targetOffsetX = { -it })
    } else {
        slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth })
    }

    Box(Modifier.fillMaxSize()) {

        content()

        if (isOpen) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onDrawerClose()
                    }
            )
        }

        Box(
            Modifier.fillMaxSize(),
            contentAlignment = alignment
        ) {
            AnimatedVisibility(
                visible = isOpen,
                enter = enter,
                exit = exit
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(width)
                        .background(AppTheme.colors.BackgroundColor)
                        .clickable(
                            // block click passing thru drawer content
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {}
                ) {
                    drawerContent()
                }
            }
        }
    }
}