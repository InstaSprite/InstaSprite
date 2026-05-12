package com.instasprite.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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