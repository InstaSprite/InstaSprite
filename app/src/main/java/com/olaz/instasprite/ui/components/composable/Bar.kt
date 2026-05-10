package com.olaz.instasprite.ui.components.composable

import androidx.compose.ui.res.stringResource
import com.olaz.instasprite.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.ui.theme.AppTheme
import com.olaz.instasprite.ui.theme.InstaSpriteTheme


@Composable
fun Bar(
    modifier: Modifier = Modifier,
    leftSlot: (@Composable () -> Unit)? = null,
    middleSlot: (@Composable () -> Unit)? = null,
    rightSlot: (@Composable () -> Unit)? = null,
    height: Dp = 56.dp,
    backgroundColor: Color = AppTheme.colors.BackgroundColor,
    space: Dp = 16.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(backgroundColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .align(Alignment.CenterStart)
        ) {
            leftSlot?.invoke()
            Spacer(modifier = Modifier.width(space))
            middleSlot?.invoke()
            Spacer(modifier = Modifier.width(space))
            rightSlot?.invoke()
        }
    }
}

@Composable
@Preview
private fun Preview() {
    InstaSpriteTheme() {
        Bar(
            leftSlot = {
                BackButton(onClick = {})
            },
            middleSlot = {
                Text(
                    text = stringResource(R.string.test_test_alo_alo),
                    color = AppTheme.colors.TextColorLight
                )
            },
            rightSlot = {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.more),
                    tint = AppTheme.colors.TextColorLight,
                    modifier = Modifier.size(32.dp)
                )
            }
        )
    }
}