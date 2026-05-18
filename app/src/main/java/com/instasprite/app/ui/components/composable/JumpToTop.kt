package com.instasprite.app.ui.components.composable

import androidx.compose.ui.res.stringResource

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun JumpToTopButton(
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    Button(
        onClick = {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.BackgroundColor
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrowup),
            contentDescription = stringResource(R.string.jump_to_top),
            tint = AppTheme.colors.TextColorLight,
            modifier = Modifier
                .size(24.dp)
                .rotate(rotation.value)
        )

    }
}