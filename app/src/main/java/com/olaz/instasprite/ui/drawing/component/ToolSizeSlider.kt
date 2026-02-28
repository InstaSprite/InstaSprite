package com.olaz.instasprite.ui.drawing.component

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
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme

@Composable
fun ToolSizeSlider(
    toolSizeValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 32.dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .background(
                color = CatppuccinUI.BackgroundColor,
            )
            .height(height)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${toolSizeValue}px",
                color = CatppuccinUI.TextColorLight,
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