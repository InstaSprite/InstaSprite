package com.instasprite.app.ui.components.composable

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.instasprite.app.R
import com.instasprite.app.ui.theme.AppTheme


@Composable
fun BackButton(
    onClick: () -> Unit,
    color: Color = AppTheme.colors.DismissButtonColor
) {
    IconButton(onClick = onClick) {
        PixelIcon(
            icon = R.drawable.ic_left_arrow,
            contentDescription = stringResource(R.string.dismiss),
            tint = color,
        )
    }
}