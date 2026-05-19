package com.instasprite.app.ui.drawing.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.instasprite.app.domain.tool.shape.CircleTool
import com.instasprite.app.domain.tool.shape.DiamondTool
import com.instasprite.app.domain.tool.shape.LineTool
import com.instasprite.app.domain.tool.shape.OvalTool
import com.instasprite.app.domain.tool.shape.RectangleTool
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun ShapeSelector(
    modifier: Modifier = Modifier,
    selectedTool: Tool,
    onShapeSelected: (Tool) -> Unit
) {
    val shapeTools = listOf(
        LineTool,
        RectangleTool,
        CircleTool,
        OvalTool,
        DiamondTool
    )

    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.BackgroundColorDarker,
                contentColor = AppTheme.colors.TextColorLight
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(36.dp)
        ) {
            PixelIcon(
                icon = selectedTool.icon,
                contentDescription = selectedTool.name,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = selectedTool.name, fontSize = 12.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(AppTheme.colors.BackgroundColor)
        ) {
            shapeTools.forEach { tool ->
                DropdownMenuItem(
                    text = { Text(tool.name, color = AppTheme.colors.TextColorLight) },
                    leadingIcon = {
                        PixelIcon(
                            icon = tool.icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = {
                        onShapeSelected(tool)
                        expanded = false
                    }
                )
            }
        }
    }
}
