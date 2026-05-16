package com.instasprite.app.ui.social.feed.component

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instasprite.app.ui.components.composable.AsyncImageView
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun ProfileImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
) {
    if (imageUrl != null) {
        Log.d("ProfileImage", "Displaying image: $imageUrl")
        AsyncImageView(
            imageUrl = imageUrl,
            altText = "Profile Image",
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            onError = { error ->
                Log.e("ProfileImage", "Error loading image: ${error}")
            },
            onSuccess = { success ->
                Log.d("ProfileImage", "Successfully loaded image")
            }
        )
    } else {
        Log.d(
            "ProfileImage",
            "Showing default icon - imageUrl: null"
        )
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = stringResource(R.string.default_profile),
            modifier = modifier.size(size),
            tint = AppTheme.colors.TextColorLight
        )
    }
}
