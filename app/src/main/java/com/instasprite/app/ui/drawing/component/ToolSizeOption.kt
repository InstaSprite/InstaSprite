package com.instasprite.app.ui.drawing.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun ToolSizeOption(
    toolSize: Int,
    onToolSizeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.BackgroundColorDarker,
                contentColor = AppTheme.colors.TextColorLight
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text(text = "$toolSize px", fontSize = 12.sp)
        }

        if (expanded) {
            ToolSizeWheelPopup(
                toolSize = toolSize,
                onDismiss = { expanded = false },
                itemHeight = 36.dp,
                onValueChange = onToolSizeChange
            )
        }
    }
}
