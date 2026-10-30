package com.instasprite.app.ui.components.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instasprite.app.R
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun CustomDialog(
    title: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)?,
    confirmButtonText: String = "OK",
    dismissButtonText: String = "Cancel",
    confirmButtonColor: Color = AppTheme.colors.green,
    scrollable: Boolean = false,
    dismissEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (dismissEnabled) onDismiss() },
        containerColor = AppTheme.colors.surfaceContainer,
        title = {
            if (title != null) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = AppTheme.colors.onSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        text = {
            val modifier = if (scrollable) {
                Modifier.verticalScroll(rememberScrollState())
            } else {
                Modifier
            }
            Column(
                modifier = modifier
            ) {
                content()
            }
        },
        confirmButton = {
            if (onConfirm != null) Button(
                onClick = onConfirm,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = confirmButtonColor)
            ) {
                Text(confirmButtonText, color = AppTheme.colors.onAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = dismissEnabled) {
                Text(dismissButtonText, color = AppTheme.colors.onSurface)
            }
        }
    )
}


@Composable
@Preview
fun CustomDialogPreview() {
    CustomDialog(
        title = "Title",
        onDismiss = {},
        onConfirm = {},
        content = {
            Text(stringResource(R.string.content))
        }
    )
}
