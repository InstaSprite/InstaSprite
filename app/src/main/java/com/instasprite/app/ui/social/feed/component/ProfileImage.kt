package com.instasprite.app.ui.social.feed.component

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.AsyncImageView
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.pixelDp
import com.instasprite.app.utils.rememberPixelPainter

@Composable
fun ProfileImage(
    imageUrl: String?,
    shape: Shape = MaterialTheme.shapes.small,
    modifier: Modifier = Modifier,
    size: Dp = 66.pixelDp,
) {
    AsyncImageView(
        imageUrl = imageUrl,
        altText = "Profile Image",
        modifier = modifier
            .size(size)
            .clip(shape),
        onError = { error ->
            Log.e("ProfileImage", "Error loading image: ${error}")
        },
        fallback = rememberPixelPainter(R.drawable.ic_default_profile),
        error = rememberPixelPainter(R.drawable.ic_default_profile),
        placeHolder = rememberPixelPainter(R.drawable.ic_default_profile)
    )

}
