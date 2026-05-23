package com.instasprite.app.ui.social.createpost.composable

import com.instasprite.app.utils.pixelDp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.instasprite.app.data.model.InputField
import com.instasprite.app.ui.components.composable.InputTextField
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun CaptionSection(
    enabled: Boolean = true,
    value: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 4,
    onValueChange: (String) -> Unit = {}
) {
    Column {
        InputTextField(
            enabled = enabled,
            value = value,
            onValueChange = onValueChange,
            maxLines = maxLines,
            inputField = InputField(label = "What's on your mind?"),
            modifier = modifier
        )

        Text(
            text = "${value.length}/2200",
            color = AppTheme.colors.Subtext0Color,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 10.pixelDp)
        )
    }
}