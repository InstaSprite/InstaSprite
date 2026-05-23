package com.instasprite.app.ui.drawing.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.domain.tool.BrushShape
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.shape.PixelShape
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun BrushShapeSelector(
    modifier: Modifier = Modifier,
    selectedShape: BrushShape,
    onShapeSelected: (BrushShape) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.BackgroundColorDarker,
                contentColor = AppTheme.colors.TextColorLight
            ),
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text(text = "Shape", fontSize = 12.sp)

            Spacer(modifier = Modifier.width(8.dp))

            PixelIcon(
                icon = selectedShape.icon,
                contentDescription = selectedShape.name,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(AppTheme.colors.BackgroundColor)
        ) {
            BrushShape.entries.forEach { shape ->
                DropdownMenuItem(
                    text = { Text(shape.name, color = AppTheme.colors.TextColorLight) },
                    leadingIcon = {
                        PixelIcon(
                            icon = shape.icon,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        onShapeSelected(shape)
                        expanded = false
                    }
                )
            }
        }
    }
}
