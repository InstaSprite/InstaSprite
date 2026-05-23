package com.instasprite.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.instasprite.app.utils.Constants.PIXEL_DP

object UiUtils {
    @Composable
    fun SetStatusBarColor(statusBarColor: Color) {
        val systemUiController = rememberSystemUiController()
        LaunchedEffect(statusBarColor) {
            systemUiController.setStatusBarColor(
                color = statusBarColor,
                darkIcons = statusBarColor.luminance() > 0.5f
            )
        }
    }
    @Composable
    fun SetNavigationBarColor(statusBarColor: Color) {
        val systemUiController = rememberSystemUiController()
        LaunchedEffect(statusBarColor) {
            systemUiController.setNavigationBarColor(
                color = statusBarColor,
                darkIcons = statusBarColor.luminance() > 0.5f
            )
        }
    }
}

inline val Float.pixelDp: Dp
    get() = (this * PIXEL_DP).dp

inline val Int.pixelDp: Dp
    get() = (this * PIXEL_DP).dp

inline val Double.pixelDp: Dp
    get() = (this * PIXEL_DP).dp

@Composable
fun rememberIconBitmap(
    @DrawableRes resId: Int?,
    trimBorderAmount: Int = 0
): ImageBitmap? {
    val context = LocalContext.current

    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, resId) {
        value = resId?.let {
            val drawable = ContextCompat.getDrawable(context, it)

            drawable
                ?.toBitmap()
                ?.trimBorder(trimBorderAmount)
                ?.asImageBitmap()
        }
    }

    return imageBitmap
}

fun Context.loadIconBitmap(
    @DrawableRes resId: Int,
    trimBorderAmount: Int = 0
): Bitmap? {
    val drawable = ContextCompat.getDrawable(this, resId) ?: return null

    val bitmap = drawable.toBitmap()
        .trimBorder(trimBorderAmount)

    return bitmap
}

@Composable
fun rememberPixelPainter(@DrawableRes resId: Int): Painter {
    val context = LocalContext.current

    return remember(resId) {
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)

        BitmapPainter(
            image = bitmap.asImageBitmap(),
            filterQuality = FilterQuality.None
        )
    }
}

fun Bitmap.trimBorder(pixels: Int = 1): Bitmap {
    val newWidth = width - pixels * 2
    val newHeight = height - pixels * 2

    if (newWidth <= 0 || newHeight <= 0) return this

    return Bitmap.createBitmap(
        this,
        pixels,
        pixels,
        newWidth,
        newHeight
    )
}

@Composable
fun Modifier.noRippleClickable(onClick: () -> Unit) = clickable(
    indication = null,
    interactionSource = remember { MutableInteractionSource() },
    onClick = onClick
)