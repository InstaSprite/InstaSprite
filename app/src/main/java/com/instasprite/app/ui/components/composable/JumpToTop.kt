package com.instasprite.app.ui.components.composable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import com.instasprite.app.R
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.pixelDp

@Composable
fun JumpToTopButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.surfaceInset
        ),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(1.pixelDp),
        modifier = modifier.size(32.pixelDp)
    ) {
        PixelIcon(
            icon = R.drawable.ic_up_arrow,
            contentDescription = stringResource(R.string.jump_to_top),
            tint = AppTheme.colors.onSurface,
            modifier = Modifier
                .rotate(rotation.value)
        )

    }
}
