package com.instasprite.app.ui.gallery.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instasprite.app.ui.components.dialog.CustomDialog
import com.instasprite.app.R
import com.instasprite.app.ui.gallery.contract.GalleryExportFormat
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme

@Composable
internal fun ExportOptionsDialog(
    hasAnimation: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSelect: (GalleryExportFormat) -> Unit
) {
    CustomDialog(
        title = stringResource(R.string.export_title),
        onDismiss = onDismiss,
        onConfirm = null,
        dismissButtonText = stringResource(R.string.cancel),
        dismissEnabled = !isLoading,
        scrollable = true,
        content = {
            Column {
                if (isLoading) {
                    CircularProgressIndicator(color = AppTheme.colors.purple)
                } else {
                    val options = listOf(
                        GalleryExportFormat.IMAGE to R.string.export_option_image,
                        GalleryExportFormat.GIF to R.string.export_option_gif,
                        GalleryExportFormat.SPRITE_SHEET to R.string.export_option_sheet,
                        GalleryExportFormat.PROJECT to R.string.export_option_project
                    )
                    options.forEach { (format, label) ->
                        if (format != GalleryExportFormat.SPRITE_SHEET || hasAnimation) {
                            TextButton(onClick = { onSelect(format) }, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(label), color = AppTheme.colors.onSurface)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Preview(name = "Animated sprite export")
@Composable
private fun ExportOptionsDialogPreview() {
    InstaSpriteTheme { ExportOptionsDialog(true, false, {}, {}) }
}

@Preview(name = "Still sprite export")
@Composable
private fun StillExportOptionsDialogPreview() {
    InstaSpriteTheme { ExportOptionsDialog(false, false, {}, {}) }
}
