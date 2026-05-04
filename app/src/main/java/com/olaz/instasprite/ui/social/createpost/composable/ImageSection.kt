package com.olaz.instasprite.ui.social.createpost.composable

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.R
import com.olaz.instasprite.ui.components.composable.AsyncImageView
import com.olaz.instasprite.ui.theme.CatppuccinUI

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
                .clip(RoundedCornerShape(8.dp))
                .background(CatppuccinUI.BackgroundColor)
                .clickable(onClick = onClick)
        )

    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(CatppuccinUI.BackgroundColor)
                .clickable(onClick = onClick)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Add, contentDescription = "Add Image",
                    tint = CatppuccinUI.AccentButtonColor,
                    modifier = Modifier.size(62.dp)
                )
                Text(
                    text = stringResource(R.string.select_sprites),
                    color = CatppuccinUI.TextColorLight
                )
            }
        }
    }

}