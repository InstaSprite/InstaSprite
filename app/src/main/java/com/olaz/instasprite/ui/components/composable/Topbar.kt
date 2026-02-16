package com.olaz.instasprite.ui.components.composable

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme


@Composable
fun TopBar(
    leftSlot: (@Composable () -> Unit)? = null,
    middleSlot: (@Composable () -> Unit)? = null,
    rightSlot: (@Composable () -> Unit)? = null,
    space: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(CatppuccinUI.BackgroundColor)
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
        TopBar(
            leftSlot = {
                BackButton(onClick = {})
            },
            middleSlot = {
                Text(
                    text = "Test test alo alo",
                    color = CatppuccinUI.TextColorLight
                )
            },
            rightSlot = {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = CatppuccinUI.TextColorLight,
                    modifier = Modifier.size(32.dp)
                )
            }
        )
    }
}