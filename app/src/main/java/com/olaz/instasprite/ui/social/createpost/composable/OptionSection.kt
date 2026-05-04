package com.olaz.instasprite.ui.social.createpost.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.olaz.instasprite.ui.theme.CatppuccinUI


@Composable
fun OptionSection(
    isCommentChecked: Boolean,
    modifier: Modifier = Modifier,
    onEnableCommentChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Enable Comments",
            color = CatppuccinUI.TextColorLight,
            style = MaterialTheme.typography.bodyMedium
        )
        Checkbox(
            checked = isCommentChecked,
            onCheckedChange = onEnableCommentChange,
            colors = CheckboxDefaults.colors(
                checkedColor = CatppuccinUI.SelectedColor,
                uncheckedColor = CatppuccinUI.Foreground2Color,
                checkmarkColor = CatppuccinUI.TextColorDark
            )
        )
    }
}
