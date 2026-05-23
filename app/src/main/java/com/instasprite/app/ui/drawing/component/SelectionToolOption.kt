package com.instasprite.app.ui.drawing.component

import com.instasprite.app.utils.pixelDp

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun SelectionToolOption(
    isAppendMode: Boolean,
    onAppendModeToggle: (Boolean) -> Unit,
    onClearSelect: () -> Unit,
    onInvertSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Popup(
            alignment = Alignment.Center,
            properties = PopupProperties(dismissOnClickOutside = false)
        ) {
            Row(
                modifier = Modifier
                    .height(24.pixelDp)
                    .clip(MaterialTheme.shapes.small)
                    .background(AppTheme.colors.BackgroundColor)
                    .padding(horizontal = 2.pixelDp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.pixelDp)
            ) {
                TextButton(
                    onClick = onClearSelect,
                    contentPadding = PaddingValues(horizontal = 6.pixelDp)
                ) {
                    Text(
                        text = stringResource(R.string.clear),
                        color = AppTheme.colors.DismissButtonColor
                    )
                }

                TextButton(
                    onClick = onInvertSelect,
                    contentPadding = PaddingValues(horizontal = 6.pixelDp)
                ) {
                    Text(
                        text = stringResource(R.string.invert),
                        color = AppTheme.colors.TextColorLight
                    )
                }

                TextButton(
                    onClick = { onAppendModeToggle(!isAppendMode) },
                    contentPadding = PaddingValues(horizontal = 6.pixelDp)
                ) {
                    Checkbox(
                        checked = isAppendMode,
                        onCheckedChange = onAppendModeToggle,
                        modifier = Modifier.width(24.pixelDp)
                    )
                    Text(
                        text = stringResource(R.string.append),
                        color = AppTheme.colors.TextColorLight
                    )
                }
            }
        }
    }
}


@Preview
@Composable
private fun Preview() {
    SelectionToolOption(
        isAppendMode = true,
        onAppendModeToggle = {},
        onClearSelect = {},
        onInvertSelect = {}
    )
}