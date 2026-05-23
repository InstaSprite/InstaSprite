package com.instasprite.app.ui.drawing.component

import com.instasprite.app.utils.pixelDp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.domain.tool.selection.LassoSelectionTool
import com.instasprite.app.domain.tool.selection.MagicWandTool
import com.instasprite.app.domain.tool.selection.RectangleSelectionTool
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.shape.PixelShape
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun SelectionModeSelector(
    modifier: Modifier = Modifier,
    selectedTool: Tool,
    onSelectionToolSelected: (Tool) -> Unit
) {
    val selectionTools = listOf(
        RectangleSelectionTool,
        LassoSelectionTool,
        MagicWandTool
    )

    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.BackgroundColorDarker,
                contentColor = AppTheme.colors.TextColorLight
            ),
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = 8.pixelDp, vertical = 1.pixelDp),
            modifier = Modifier.height(24.pixelDp)
        ) {
            PixelIcon(
                icon = selectedTool.icon,
                contentDescription = selectedTool.name,
                )
            Spacer(modifier = Modifier.width(6.pixelDp))
            Text(text = selectedTool.name, fontSize = 12.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(AppTheme.colors.BackgroundColor)
        ) {
            selectionTools.forEach { tool ->
                DropdownMenuItem(
                    text = { Text(tool.name, color = AppTheme.colors.TextColorLight) },
                    leadingIcon = {
                        PixelIcon(
                            icon = tool.icon,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        onSelectionToolSelected(tool)
                        expanded = false
                    }
                )
            }
        }
    }
}
