package com.instasprite.app.ui.social.createpost.composable

import com.instasprite.app.utils.pixelDp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.domain.model.InputField
import com.instasprite.app.ui.components.composable.InputTextField
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import androidx.compose.ui.text.input.ImeAction

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HashtagSection(
    enabled: Boolean = true,
    inputValue: String,
    hashtags: List<String>,
    onValueChange: (String) -> Unit,
    onAddHashtag: () -> Unit,
    onRemoveHashtag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        InputTextField(
            enabled = enabled,
            value = inputValue,
            onValueChange = {
                if (it.endsWith(" ") || it.endsWith("\n")) {
                    onAddHashtag()
                } else {
                    onValueChange(it)
                }
            },
            inputField = InputField(label = stringResource(R.string.add_tags)),
            maxLines = 1,
            imeAction = ImeAction.Done,
            onImeAction = onAddHashtag,
            trailingIcon = {
                IconButton(onClick = onAddHashtag) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_tag), tint = AppTheme.colors.LinkColor)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (hashtags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.pixelDp),
                horizontalArrangement = Arrangement.spacedBy(6.pixelDp),
                verticalArrangement = Arrangement.spacedBy(2.pixelDp)
            ) {
                hashtags.forEach { tag ->
                    AssistChip(
                        onClick = { onRemoveHashtag(tag) },
                        label = { Text("#$tag") },
                        trailingIcon = {
                            PixelIcon(
                                icon = R.drawable.ic_close,
                                contentDescription = stringResource(R.string.remove_tag),
                                tint = AppTheme.colors.DismissButtonColor,
                                scale = 0.5f
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = AppTheme.colors.BackgroundColor,
                            labelColor = AppTheme.colors.SelectedColor,
                            trailingIconContentColor = AppTheme.colors.Subtext0Color
                        ),
                        border = BorderStroke(1.pixelDp, AppTheme.colors.TextColorLight)
                    )
                }
            }
        }
    }
}
