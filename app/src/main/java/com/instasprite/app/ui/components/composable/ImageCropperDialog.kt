package com.instasprite.app.ui.components.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.ui.theme.AppTheme
import com.mr0xf00.easycrop.AspectRatio
import com.mr0xf00.easycrop.CropperStyle
import com.mr0xf00.easycrop.CropperStyleGuidelines
import com.mr0xf00.easycrop.ImageCropper
import com.mr0xf00.easycrop.ui.ImageCropperDialog

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Rect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropperDialog(
    imageCropper: ImageCropper,
    aspectRatio: AspectRatio = AspectRatio(1, 1),
    autoZoom: Boolean = true,
    aspectLock: Boolean = false
) {
    val cropState = imageCropper.cropState ?: return

    BackHandler {
        cropState.done(accept = false)
    }

    LaunchedEffect(cropState, aspectLock) {
        val size = cropState.src.size
        val minDim = minOf(size.width, size.height).toFloat()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        cropState.region = Rect(
            left = centerX - minDim / 2f,
            top = centerY - minDim / 2f,
            right = centerX + minDim / 2f,
            bottom = centerY + minDim / 2f
        )
        cropState.aspectLock = aspectLock
    }

    ImageCropperDialog(
        state = cropState,
        style = CropperStyle(
            backgroundColor = AppTheme.colors.BackgroundColorDarker,
            overlay = AppTheme.colors.BackgroundColorDarker.copy(alpha = 0.7f),
            rectColor = AppTheme.colors.SelectedColor,
            guidelines = CropperStyleGuidelines(
                color = AppTheme.colors.Subtext0Color.copy(alpha = 0.5f)
            ),
            aspects = listOf(aspectRatio),
            shapes = null,
            autoZoom = autoZoom,
        ),
        topBar = { state ->
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.crop_image),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.TextColorLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { state.done(accept = false) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = AppTheme.colors.TextColorLight
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { state.reset() }) {
                        Icon(
                            painter = painterResource(com.mr0xf00.easycrop.R.drawable.restore),
                            contentDescription = null,
                            tint = AppTheme.colors.TextColorLight
                        )
                    }
                    IconButton(
                        onClick = { state.done(accept = true) },
                        enabled = !state.accepted
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = stringResource(R.string.save),
                            tint = if (!state.accepted)
                                AppTheme.colors.AccentButtonColor
                            else
                                AppTheme.colors.InactiveColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.BackgroundColorDarker
                )
            )
        }
    )
}
