package com.olaz.instasprite.ui.drawing.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.ui.components.composable.TitledBox
import com.olaz.instasprite.ui.theme.AppTheme

@Composable
fun SelectionToolOption(
    isVisible: Boolean,
    isAppendMode: Boolean,
    onAppendModeToggle: (Boolean) -> Unit,
    onClearSelect: () -> Unit,
    onInvertSelect: () -> Unit,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = ExitTransition.None,
        ) {
        TitledBox(
            title = "Selection Option",
            titleBackgroundColor = AppTheme.colors.BackgroundColorDarker
        ) {
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .background(AppTheme.colors.BackgroundColor)
            )
            {
                TextButton(
                    onClick = onClearSelect,
                    modifier = Modifier
                        .background(AppTheme.colors.BackgroundColor)
                ) {
                    Text(
                        text = "Clear",
                        color = AppTheme.colors.DismissButtonColor
                    )
                }

                TextButton(
                    onClick = onInvertSelect,
                    modifier = Modifier
                        .background(AppTheme.colors.BackgroundColor)
                ) {
                    Text(
                        text = "Invert",
                        color = AppTheme.colors.TextColorLight
                    )
                }

                TextButton(
                    onClick = { onAppendModeToggle(!isAppendMode) },
                    modifier = Modifier
                        .background(AppTheme.colors.BackgroundColor)
                ) {
                    Checkbox(
                        checked = isAppendMode,
                        onCheckedChange = onAppendModeToggle,
                        modifier = Modifier
                            .background(AppTheme.colors.BackgroundColor)
                            .width(35.dp)
                    )
                    Text(
                        text = "Append",
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
        isVisible = true,
        isAppendMode = true,
        onAppendModeToggle = {},
        onClearSelect = {}
    ) { }
}