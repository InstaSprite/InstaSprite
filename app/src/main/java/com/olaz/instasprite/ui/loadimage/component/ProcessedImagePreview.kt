package com.olaz.instasprite.ui.loadimage.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.olaz.instasprite.ui.theme.CatppuccinTypography
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import com.olaz.instasprite.utils.drawCheckerboard

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
                .background(CatppuccinUI.BackgroundColorDarker)
                .clickable(enabled = true, onClick = onLaunchImagePicker),
            contentAlignment = Alignment.Center
        ) {

            if (processedBitmap != null) {
                Image(
                    bitmap = processedBitmap.asImageBitmap(),
                    contentDescription = "Processed Image",
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
                        contentDescription = "Add image",
                        tint = CatppuccinUI.CurrentPalette.Peach,
                        modifier = Modifier.size(50.dp)
                    )

                    Text(
                        "Tap to select image",
                        color = CatppuccinUI.Subtext0Color,
                        style = CatppuccinTypography.bodyMedium
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CatppuccinUI.BackgroundColorDarker.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CatppuccinUI.AccentButtonColor)
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