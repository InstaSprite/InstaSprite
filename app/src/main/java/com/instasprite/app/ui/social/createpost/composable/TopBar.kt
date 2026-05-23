package com.instasprite.app.ui.social.createpost.composable

import com.instasprite.app.utils.pixelDp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun TopBar(
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.pixelDp)
            .background(AppTheme.colors.BackgroundColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.pixelDp)
                .align(Alignment.CenterStart)
        ) {
            IconButton(
                onClick = onDismiss
            ) {
                PixelIcon(
                    icon = R.drawable.ic_left_arrow,
                    contentDescription = stringResource(R.string.dismiss),
                    tint = AppTheme.colors.DismissButtonColor,
                )
            }
        }
    }
}