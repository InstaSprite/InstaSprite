package com.instasprite.app.ui.components.composable

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.instasprite.app.R
import androidx.compose.material3.MaterialTheme
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.Constants.PIXEL_DP
import com.instasprite.app.utils.noRippleClickable
import com.instasprite.app.utils.pixelDp
import kotlin.math.roundToInt

enum class FabMenuAlignment {
    Start,
    Center,
    End
}

data class FabMenuItem(
    @DrawableRes val icon: Int,
    val label: String,
    val onClick: () -> Unit
)

data class FabMenuColors(
    val fab: Color,
    val fabIcon: Color,
    val itemIcon: Color,
    val itemBackground: Color,
    val itemIconBackground: Color,
    val label: Color,
    val scrim: Color,
) {
    companion object {
        @Composable
        fun defaults(
            fab: Color = AppTheme.colors.TextColorLight,
            fabIcon: Color = AppTheme.colors.TextColorDark,
            itemIcon: Color = AppTheme.colors.LinkColor,
            itemBackground: Color = AppTheme.colors.BackgroundColor,
            itemIconBackground: Color = AppTheme.colors.Foreground0Color,
            label: Color = AppTheme.colors.TextColorLight,
            scrim: Color = Color.Black.copy(alpha = 0.8f),
        ) = FabMenuColors(fab, fabIcon, itemIcon, itemBackground, itemIconBackground, label, scrim)
    }
}

@Composable
fun ExpandableFabMenu(
    items: List<FabMenuItem>,
    modifier: Modifier = Modifier,
    alignment: FabMenuAlignment = FabMenuAlignment.Center,
    itemWidth: Dp = 150.pixelDp,
    itemSpacing: Dp = 4.pixelDp,
    animationDurationMs: Int = 300,
    itemStaggerDelayMs: Int = 50,
    mainFabSize: Dp = 32.pixelDp,
    mainFabIconSize: Dp = 20.pixelDp,
    miniFabSize: Dp = 32.pixelDp,
    colors: FabMenuColors = FabMenuColors.defaults(),
) {
    val density = LocalDensity.current
    var expanded by remember { mutableStateOf(false) }
    val expandedState = remember { MutableTransitionState(false) }
    expandedState.targetState = expanded

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 135f else 0f,
        animationSpec = tween(durationMillis = animationDurationMs, easing = FastOutSlowInEasing)
    )
    var fabOffset by remember { mutableStateOf(Offset.Zero) }
    var fabSize by remember { mutableStateOf(IntSize.Zero) }
    var columnSize by remember { mutableStateOf(IntSize.Zero) }

    val itemOffset by remember(
        fabOffset,
        fabSize,
        columnSize,
        alignment,
        items.size
    ) {
        derivedStateOf {
            val itemSpacingPx = with(density) {
                itemSpacing.roundToPx()
            }

            val alignedX = when (alignment) {
                FabMenuAlignment.Start ->
                    fabOffset.x.roundToInt()

                FabMenuAlignment.Center ->
                    fabOffset.x.roundToInt() + (fabSize.width - columnSize.width) / 2

                FabMenuAlignment.End ->
                    fabOffset.x.roundToInt() + fabSize.width - columnSize.width
            }

            val alignedY =
                fabOffset.y.roundToInt() -
                        columnSize.height -
                        itemSpacingPx * (items.size + 1) * PIXEL_DP

            IntOffset(alignedX, alignedY.toInt())
        }
    }

    // keep Popup alive doing animation stuff
    if (expandedState.currentState || expandedState.targetState) {
        val transition = rememberTransition(expandedState, label = "fab_menu")

        val scrimAlpha by transition.animateFloat(
            transitionSpec = {
                tween(
                    durationMillis = animationDurationMs,
                    easing = FastOutSlowInEasing
                )
            },
            label = "scrim_alpha"
        ) { isExpanded -> if (isExpanded) 1f else 0f }

        Popup(
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = true)
        ) {
            Box(
                modifier = Modifier

                    .fillMaxSize()
                    .background(colors.scrim.copy(alpha = colors.scrim.alpha * scrimAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { expanded = false }
                    ),
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(itemSpacing),
                    modifier = Modifier
                        .wrapContentWidth()
                        .onGloballyPositioned { coordinates ->
                            columnSize = coordinates.size
                        }
                        .offset {
                            itemOffset
                        }
                ) {
                    items.forEachIndexed { index, item ->
                        val delay = (items.size - 1 - index) * itemStaggerDelayMs
                        val animProgress by transition.animateFloat(
                            transitionSpec = {
                                tween(
                                    durationMillis = animationDurationMs,
                                    delayMillis = delay,
                                    easing = FastOutSlowInEasing
                                )
                            }
                        ) { isExpanded -> if (isExpanded) 1f else 0f }

                        FabItemView(
                            item = item,
                            animProgress = animProgress,
                            fabSize = miniFabSize,
                            colors = colors,
                            onDismiss = { expanded = false },
                            modifier = Modifier.width(itemWidth)
                        )
                    }
                }
            }
        }
    }

    FloatingActionButton(
        onClick = { expanded = !expanded },
        containerColor = colors.fab,
        modifier = modifier
            .size(mainFabSize)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.localToWindow(Offset.Zero)
                fabSize = coordinates.size
                fabOffset = Offset(
                    x = position.x,
                    y = position.y
                )
            }
    ) {
        PixelIcon(
            icon = R.drawable.ic_plus,
            contentDescription = if (expanded) "Close menu" else "Open menu",
            tint = colors.fabIcon,
            modifier = Modifier
                .rotate(rotation)
        )
    }
}

@Composable
private fun FabItemView(
    item: FabMenuItem,
    animProgress: Float,
    modifier: Modifier = Modifier,
    fabSize: Dp = 32.pixelDp,
    colors: FabMenuColors,
    onDismiss: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .alpha(animProgress)
            .scale(0.4f + 0.6f * animProgress)
            .background(
                color = colors.itemBackground,
                shape = MaterialTheme.shapes.small
            )
            .noRippleClickable(
                onClick = {
                    onDismiss()
                    item.onClick()
                }
            )
    ) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(4.pixelDp)
                .size(fabSize)
                .background(shape = MaterialTheme.shapes.small, color = colors.itemIconBackground)
        ) {
            PixelIcon(
                icon = item.icon,
                contentDescription = item.label,
                tint = colors.itemIcon,
            )
        }

        Text(
            text = item.label,
            color = colors.label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.pixelDp, vertical = 4.pixelDp)
        )
    }
}

@Preview
@Composable
private fun FabItemViewPreview() {
    InstaSpriteTheme() {
        FabItemView(
            item = FabMenuItem(R.drawable.ic_edit, "New Canvas") {},
            animProgress = 1f,
            colors = FabMenuColors.defaults(),
            onDismiss = {}
        )
    }
}


@Preview
@Composable
private fun Preview() {
    InstaSpriteTheme() {
        Box(
            modifier = Modifier
                .size(166.pixelDp, 234.pixelDp)
                .background(AppTheme.colors.BackgroundColorDarker),
            contentAlignment = Alignment.BottomCenter
        ) {
            ExpandableFabMenu(
                items = listOf(

                    FabMenuItem(R.drawable.ic_edit, "Load Canvas") {},
                    FabMenuItem(R.drawable.ic_canvas, "Load Image") {},
                ),
                modifier = Modifier.padding(10.pixelDp)
            )
        }
    }
}