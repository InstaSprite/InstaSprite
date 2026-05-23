package com.instasprite.app.ui.social.createpost.composable

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.AsyncImageView
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun ImageSection(
    imageUri: Uri?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {

    LocalContext.current

    if (imageUri != null) {
        AsyncImageView(
            imageUrl = imageUri.toString(),
            altText = "Selected Image",
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(AppTheme.colors.BackgroundColor)
                .clickable(onClick = onClick)
        )

    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.medium)
                .background(AppTheme.colors.BackgroundColor)
                .clickable(onClick = onClick)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PixelIcon(
                    icon = R.drawable.ic_plus,
                    contentDescription = stringResource(R.string.add_image_1),
                    tint = AppTheme.colors.AccentButtonColor,
                    scale = 3f
                )
                Text(
                    text = stringResource(R.string.select_sprites),
                    color = AppTheme.colors.TextColorLight
                )
            }
        }
    }

}