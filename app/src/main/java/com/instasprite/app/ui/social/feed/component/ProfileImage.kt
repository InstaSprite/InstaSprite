package com.instasprite.app.ui.social.feed.component

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.AsyncImageView
import com.instasprite.app.utils.rememberPixelPainter

@Composable
fun ProfileImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
) {
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
        },
        fallback = rememberPixelPainter(R.drawable.ic_launcher),
        error = rememberPixelPainter(R.drawable.ic_launcher),
        placeHolder = rememberPixelPainter(R.drawable.ic_launcher)
    )

}
