package com.olaz.instasprite.ui.social.feed.component

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.ui.components.composable.AsyncImageView
import com.olaz.instasprite.ui.social.feed.ProfileImageState
import com.olaz.instasprite.ui.theme.AppTheme

@Composable
fun ProfileImage(
    state: ProfileImageState,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
) {
    when {
        state.isLoading -> {
            Log.d("ProfileImage", "Showing loading indicator")
            CircularProgressIndicator(
                modifier = modifier.size(size),
                color = AppTheme.colors.TextColorLight
            )
        }

        state.imageUrl != null -> {
            Log.d("ProfileImage", "Displaying image: ${state.imageUrl}")
            AsyncImageView(
                imageUrl = state.imageUrl,
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
        }

        else -> {
            Log.d(
                "ProfileImage",
                "Showing default icon - imageUrl: ${state.imageUrl}, error: ${state.error}"
            )
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Default Profile",
                modifier = modifier.size(size),
                tint = AppTheme.colors.TextColorLight
            )
        }
    }
}
