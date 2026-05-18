package com.instasprite.app.ui.components.dialog

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun CustomDialog(
    title: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmButtonText: String = "OK",
    dismissButtonText: String = "Cancel",
    confirmButtonColor: Color = AppTheme.colors.AccentButtonColor,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.DialogColor,
        shape = RoundedCornerShape(10.dp),
        title = {
            if (title != null)
            {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = AppTheme.colors.TextColorLight,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                content()
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = confirmButtonColor,)
            ) {
                Text(confirmButtonText, color = AppTheme.colors.TextColorDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissButtonText, color = AppTheme.colors.TextColorLight)
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
