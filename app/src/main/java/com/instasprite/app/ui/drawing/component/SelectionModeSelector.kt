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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.domain.tool.ToolType
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.pixelDp

@Composable
fun SelectionModeSelector(
    modifier: Modifier = Modifier,
    selectedTool: Tool,
    onSelectionToolSelected: (ToolType) -> Unit
) {
    val selectionTools = listOf(
        ToolType.RectangleSelection,
        ToolType.LassoSelection,
        ToolType.MagicWand
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
                contentDescription = stringResource(id = selectedTool.nameRes),
            )
            Spacer(modifier = Modifier.width(6.pixelDp))
            Text(text = stringResource(id = selectedTool.nameRes), fontSize = 12.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(AppTheme.colors.BackgroundColor)
        ) {
            selectionTools.forEach { tool ->
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(id = tool.nameRes),
                            color = AppTheme.colors.TextColorLight
                        )
                    },
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

