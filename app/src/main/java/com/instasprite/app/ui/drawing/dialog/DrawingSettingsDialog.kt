package com.instasprite.app.ui.drawing.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.dialog.CustomDialog
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.pixelDp

@Composable
fun DrawingSettingsDialog(
    isCursorMode: Boolean,
    showCanvasPreview: Boolean,
    onCursorModeChange: (Boolean) -> Unit,
    onShowCanvasPreviewChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors

    CustomDialog(
        title = stringResource(R.string.drawing_settings),
        onDismiss = onDismiss,
        onConfirm = onDismiss,
        confirmButtonText = stringResource(R.string.ok),
        dismissButtonText = ""
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.pixelDp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCursorModeChange(!isCursorMode) }
                    .padding(vertical = 4.pixelDp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PixelIcon(
                    icon = R.drawable.ic_cursor
                )
                Spacer(modifier = Modifier.width(8.pixelDp))
                Text(
                    text = stringResource(R.string.cursor_mode),
                    color = colors.TextColorLight,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = isCursorMode,
                    onCheckedChange = onCursorModeChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.LinkColor,
                        uncheckedColor = colors.Subtext0Color,
                        checkmarkColor = colors.TextColorDark
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowCanvasPreviewChange(!showCanvasPreview) }
                    .padding(vertical = 4.pixelDp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PixelIcon(
                    icon = R.drawable.ic_visible_on
                )
                Spacer(modifier = Modifier.width(8.pixelDp))
                Text(
                    text = stringResource(R.string.canvas_preview),
                    color = colors.TextColorLight,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = showCanvasPreview,
                    onCheckedChange = onShowCanvasPreviewChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.LinkColor,
                        uncheckedColor = colors.Subtext0Color,
                        checkmarkColor = colors.TextColorDark
                    )
                )
            }
        }
    }
}
