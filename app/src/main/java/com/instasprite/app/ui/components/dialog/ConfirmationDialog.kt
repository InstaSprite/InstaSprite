package com.instasprite.app.ui.components.dialog

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun ConfirmationDialog(
    title: String,
    text: String,
    highlightText: String,
    confirmButtonText: String = "OK",
    dismissButtonText: String = "Cancel",
    highlightTextColor: Color = AppTheme.colors.DismissButtonColor,
    hasQuestionMark: Boolean = true,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    CustomDialog(
        title = title,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText,
        confirmButtonColor = highlightTextColor,
        content = {
            Text(
                buildAnnotatedString {
                    append("$text ")
                    withStyle(style = SpanStyle(color = highlightTextColor)) {
                        append(highlightText)
                    }
                    if (hasQuestionMark) append("?")
                }
            )
        }
    )
}