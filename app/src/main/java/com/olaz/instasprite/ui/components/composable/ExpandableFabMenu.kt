package com.olaz.instasprite.ui.components.composable

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import kotlin.math.roundToInt

enum class FabMenuAlignment {
    Start,
    Center,
    End
}

data class FabMenuItem(
    val icon: ImageVector,
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
            fab: Color = CatppuccinUI.SelectedColor,
            fabIcon: Color = CatppuccinUI.TextColorDark,
            itemIcon: Color = CatppuccinUI.CurrentPalette.Blue,
            itemBackground: Color = CatppuccinUI.BackgroundColor,
            itemIconBackground: Color = CatppuccinUI.Foreground0Color,
            label: Color = CatppuccinUI.TextColorLight,
            scrim: Color = Color.Black.copy(alpha = 0.8f),
        ) = FabMenuColors(fab, fabIcon, itemIcon, itemBackground, itemIconBackground, label, scrim)
    }
}

@Composable
fun ExpandableFabMenu(
    items: List<FabMenuItem>,
    modifier: Modifier = Modifier,
    alignment: FabMenuAlignment = FabMenuAlignment.Center,
    itemWidth: Dp = 200.dp,
    itemSpacing: Dp = 12.dp,
    animationDurationMs: Int = 300,
    itemStaggerDelayMs: Int = 50,
    mainFabSize: Dp = 70.dp,
    mainFabIconSize: Dp = 30.dp,
    miniFabSize: Dp = 48.dp,
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
                        itemSpacingPx * (items.size + 1)

            IntOffset(alignedX, alignedY)
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
        shape = CircleShape,
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
        Icon(
            Icons.Filled.Add,
            contentDescription = if (expanded) "Close menu" else "Open menu",
            tint = colors.fabIcon,
            modifier = Modifier
                .size(mainFabIconSize)
                .rotate(rotation)
        )
    }
}

@Composable
private fun FabItemView(
    item: FabMenuItem,
    animProgress: Float,
    modifier: Modifier = Modifier,
    fabSize: Dp = 48.dp,
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
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                enabled = true,
                onClick = {
                    onDismiss()
                    item.onClick()
                }
            )
    ) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(6.dp)
                .size(fabSize)
                .background(shape = CircleShape, color = colors.itemIconBackground)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = colors.itemIcon,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = item.label,
            color = colors.label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Preview
@Composable
private fun FabItemViewPreview() {
    InstaSpriteTheme() {
        FabItemView(
            item = FabMenuItem(Icons.Default.Create, "New Canvas") {},
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
                .size(250.dp, 350.dp)
                .background(CatppuccinUI.BackgroundColorDarker),
            contentAlignment = Alignment.BottomCenter
        ) {
            ExpandableFabMenu(
                items = listOf(

                    FabMenuItem(Icons.Default.FolderOpen, "Load Canvas") {},
                    FabMenuItem(Icons.Default.Image, "Load Image") {},
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}