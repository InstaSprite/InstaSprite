package com.instasprite.app.ui.home.component


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Preview(showBackground = true, name = "BottomNavPanel Preview")
@Composable
fun PreviewBottomNavPanelWithCutOut() {
    // It's good practice to wrap previews in your app's theme or a default MaterialTheme
    // YourProjectTheme { // Replace with your actual theme if needed
    Box( // Create the Box environment
        modifier = Modifier
            .fillMaxSize() // Give the Box some space to see the alignment
            .background(Color.LightGray) // Optional: background for the parent Box
    ) {
        // Now you can call your BoxScope dependent composable
        BottomNavPanelWithCutOut()
    }
    // }
}

@Composable
fun BoxScope.BottomNavPanelWithCutOut() {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(64.dp)
            .clip(
                BottomNavShape(
                    dockRadius = with(LocalDensity.current) { 45.dp.toPx() },
                ),
            ) // Apply the custom shape
            .background(Color.Blue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 56.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Navigation icons (left and right of the cutout)
        }
    }
}

class BottomNavShape(
    private val dockRadius: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val baseRect = Path().apply {
            addRect(
                Rect(Offset.Zero, Size(size.width, size.height))
            )
        }

        val rect1 = Path().apply {
            addRect(
                Rect(Offset.Zero, Offset(size.width / 2 - dockRadius + 4f, size.height)),
            )
        }

        val rect1A = Path().apply {
            addRoundRect(
                RoundRect(
                    Rect(Offset.Zero, Offset(size.width / 2 - dockRadius + 4f, size.height)),
                    topRight = CornerRadius(32f, 32f),
                ),
            )
        }

        val rect1B = Path.combine(
            operation = PathOperation.Difference,
            path1 = rect1,
            path2 = rect1A,
        )

        val rect2 = Path().apply {
            addRoundRect(
                RoundRect(
                    Rect(
                        Offset(size.width / 2 + dockRadius - 4f, 0f),
                        Offset(size.width, size.height)
                    ),
                ),
            )
        }

        val rect2A = Path().apply {
            addRoundRect(
                RoundRect(
                    Rect(
                        Offset(size.width / 2 + dockRadius - 4f, 0f),
                        Offset(size.width, size.height)
                    ),
                    topLeft = CornerRadius(32f, 32f),
                ),
            )
        }

        val rect2B = Path.combine(
            operation = PathOperation.Difference,
            path1 = rect2,
            path2 = rect2A,
        )

        val circle = Path().apply {
            addOval(
                Rect(
                    Offset(size.width / 2 - dockRadius, -dockRadius),
                    Offset(size.width / 2 + dockRadius, dockRadius),
                ),
            )
        }

        val path1 = Path.combine(
            operation = PathOperation.Difference,
            path1 = baseRect,
            path2 = circle,
        )

        val path2 = Path.combine(
            operation = PathOperation.Difference,
            path1 = path1,
            path2 = rect1B,
        )

        val path = Path.combine(
            operation = PathOperation.Difference,
            path1 = path2,
            path2 = rect2B,
        )

        return Outline.Generic(path)
    }
}