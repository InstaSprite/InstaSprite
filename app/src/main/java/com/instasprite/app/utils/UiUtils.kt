package com.instasprite.app.utils

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.accompanist.systemuicontroller.rememberSystemUiController

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

@Composable
fun rememberIconBitmap(
    @DrawableRes resId: Int?
): ImageBitmap? {
    val context = LocalContext.current

    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, resId) {
        value = resId?.let {
            val drawable = ContextCompat.getDrawable(context, it)
            drawable?.toBitmap(64, 64)?.asImageBitmap()
        }
    }

    return imageBitmap
}