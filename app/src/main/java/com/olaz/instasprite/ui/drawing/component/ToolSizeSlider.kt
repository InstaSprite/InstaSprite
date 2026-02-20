package com.olaz.instasprite.ui.drawing.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.ui.theme.CatppuccinUI

@Composable
fun ToolSizeSlider(
    toolSizeValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(35.dp)
            .background(
                color = CatppuccinUI.BackgroundColor,
            )

    ) {
        Text(
            text = "${toolSizeValue}px",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .width(86.dp)
                .padding(start = 32.dp)
                .align(alignment = Alignment.Top)
        )

        Slider(
            value = toolSizeValue.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8,
            modifier = Modifier
                .height(30.dp)
                .weight(1f)
                .fillMaxWidth()
                .align(alignment = Alignment.CenterVertically)
                .padding(start = 10.dp, end = 32.dp)
        )
    }
}