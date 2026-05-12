package com.instasprite.app.ui.components.composable

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.instasprite.app.ui.theme.AppTheme


@Composable
fun BackButton(
    onClick: () -> Unit,
    color: Color = AppTheme.colors.DismissButtonColor
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = stringResource(R.string.dismiss),
            tint = color,
            modifier = Modifier.size(32.dp)
        )
    }
}