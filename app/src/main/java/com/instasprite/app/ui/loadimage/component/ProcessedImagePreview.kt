package com.instasprite.app.ui.loadimage.component

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.drawCheckerboard

@Composable
fun ProcessedImagePreview(
    onLaunchImagePicker: () -> Unit,
    processedBitmap: Bitmap?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .weight(1f)
                .let {
                    if (processedBitmap != null) {
                        it.drawCheckerboard(processedBitmap.width, processedBitmap.height)
                    } else it
                }
                .background(AppTheme.colors.BackgroundColorDarker)
                .clickable(enabled = true, onClick = onLaunchImagePicker),
            contentAlignment = Alignment.Center
        ) {

            if (processedBitmap != null) {
                Image(
                    bitmap = processedBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.processed_image),
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (!isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = stringResource(R.string.add_image),
                        tint = AppTheme.colors.WarningColor,
                        modifier = Modifier.size(50.dp)
                    )

                    Text(
                        stringResource(R.string.tap_to_select_image),
                        color = AppTheme.colors.Subtext0Color,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppTheme.colors.BackgroundColorDarker.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppTheme.colors.AccentButtonColor)
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    InstaSpriteTheme {
        ProcessedImagePreview(
            onLaunchImagePicker = {},
            processedBitmap = null,
            isLoading = false
        )
    }
}