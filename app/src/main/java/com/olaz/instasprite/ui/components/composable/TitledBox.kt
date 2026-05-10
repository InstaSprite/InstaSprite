package com.olaz.instasprite.ui.components.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olaz.instasprite.ui.theme.AppTheme

@Composable
fun TitledBox(
    title: String,
    modifier: Modifier = Modifier,
    titleBackgroundColor: Color = Color.Unspecified,
    titleColor: Color = AppTheme.colors.TextColorLight,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
//                .padding(16.dp)
        ) {
            content()
        }

        Text(
            text = title,
            color = titleColor,
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 12.dp, y = (-16).dp)
                .background(titleBackgroundColor)
                .padding(horizontal = 6.dp)
        )
    }
}

@Composable
@Preview
private fun Preview() {
    TitledBox(
        title = "Title",
        titleBackgroundColor = AppTheme.colors.BackgroundColor,
        modifier = Modifier
    ) {
        Text(text = "Test")
    }
}