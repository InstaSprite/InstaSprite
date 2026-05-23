package com.instasprite.app.ui.components.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade

@Composable
fun AsyncImageView(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth,
    altText: String = "Image",
    placeHolder : Painter? = null,
    fallback : Painter? = null,
    error : Painter? = null,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null
) {
    val context = LocalContext.current
    val imageRequest = remember(imageUrl, context) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .allowHardware(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    AsyncImage(
        model = imageRequest,
        contentDescription = altText,
        filterQuality = FilterQuality.None,
        contentScale = contentScale,
        placeholder = placeHolder,
        fallback = fallback,
        error = error,
        onError = onError,
        onLoading = onLoading,
        onSuccess = onSuccess,
        modifier = modifier
    )
}