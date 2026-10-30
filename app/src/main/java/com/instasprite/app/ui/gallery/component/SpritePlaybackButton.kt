package com.instasprite.app.ui.gallery.component

import androidx.compose.foundation.clickable
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme

@Composable
internal fun SpritePlaybackButton(playing: Boolean, onClick: () -> Unit) {
    PixelIcon(
        if (playing) R.drawable.ic_animation_stop else R.drawable.ic_arrow_head_right,
        contentDescription = stringResource(if (playing) R.string.animation_stop else R.string.animation_play),
        tint = if (playing) AppTheme.colors.red else AppTheme.colors.blue,
        modifier = Modifier.clickable { onClick() }
    )
}

@Preview
@Composable
private fun SpritePlaybackButtonPreview() {
    InstaSpriteTheme { SpritePlaybackButton(false, {}) }
}
