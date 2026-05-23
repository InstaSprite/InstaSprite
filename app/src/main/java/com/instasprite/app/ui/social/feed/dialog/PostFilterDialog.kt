package com.instasprite.app.ui.social.feed.dialog


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import com.instasprite.app.ui.components.dialog.CustomDialog

import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.ui.social.feed.PostFilter
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun PostFilterDialog(
    onDismiss: () -> Unit,
    onFilterSelected: (PostFilter) -> Unit,
    currentFilter: PostFilter,
) {
    LocalContext.current
    val options = mapOf(
        stringResource(R.string.filter_follow) to PostFilter.Follow,
        stringResource(R.string.filter_recent) to PostFilter.Recent
    )

    val selectedOption = remember(currentFilter) { mutableStateOf(currentFilter) }

    CustomDialog(
        title = stringResource(R.string.filter_posts),
        onDismiss = onDismiss,
        onConfirm = onDismiss,
        confirmButtonText = "",
        dismissButtonText = "",
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .selectableGroup()
        ) {
            options.forEach { (label, filter) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .selectable(
                            selected = filter == selectedOption.value,
                            onClick = {
                                selectedOption.value = filter
                                onFilterSelected(filter)
                                onDismiss()
                            },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = filter == selectedOption.value,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AppTheme.colors.SelectedColor,
                            unselectedColor = AppTheme.colors.Foreground2Color
                        ),
                        onClick = null
                    )
                    Text(
                        text = label,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}
