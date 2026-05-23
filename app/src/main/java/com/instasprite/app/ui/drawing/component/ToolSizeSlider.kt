package com.instasprite.app.ui.drawing.component

import com.instasprite.app.utils.pixelDp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme

@Composable
fun ToolSizeSlider(
    toolSizeValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 22.pixelDp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .background(
                color = AppTheme.colors.BackgroundColor,
            )
            .height(height)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${toolSizeValue}px",
                color = AppTheme.colors.TextColorLight,
            )
        }


        Slider(
            value = toolSizeValue.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8,
            modifier = Modifier
                .weight(5f)
                .align(alignment = Alignment.CenterVertically)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    InstaSpriteTheme() {
        ToolSizeSlider(toolSizeValue = 1, onValueChange = {})
    }
}