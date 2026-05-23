package com.instasprite.app.ui.social.createpost.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.CustomTextField
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme

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
        CustomTextField(
            enabled = enabled,
            value = inputValue,
            onValueChange = {
                if (it.endsWith(" ") || it.endsWith("\n")) {
                    onAddHashtag()
                } else {
                    onValueChange(it)
                }
            },
            label = "Add tags (space to separate)",
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )

        if (hashtags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = AppTheme.colors.Foreground2Color,
                            labelColor = AppTheme.colors.TextColorLight,
                            trailingIconContentColor = AppTheme.colors.Subtext0Color
                        ),
                        border = BorderStroke(1.dp, AppTheme.colors.Foreground1Color)
                    )
                }
            }
        }
    }
}
