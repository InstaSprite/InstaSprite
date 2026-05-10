package com.olaz.instasprite.ui.gallery.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.ui.gallery.SpriteListOrder
import com.olaz.instasprite.ui.theme.AppTheme


@Composable
fun SelectSortOptionDialog(
    onSortOrderSelected: (SpriteListOrder) -> Unit,
    spriteListOrder: SpriteListOrder,
    onDismiss: () -> Unit,
) {
    val options = mapOf(
        "A - Z" to SpriteListOrder.Name,
        "Z - A" to SpriteListOrder.NameDesc,
        "Date Created" to SpriteListOrder.DateCreated,
        "Date Created Desc" to SpriteListOrder.DateCreatedDesc,
        "Date Modified" to SpriteListOrder.LastModified,
        "Date Modified Desc" to SpriteListOrder.LastModifiedDesc
    )

    val selectedOption = remember { mutableStateOf(spriteListOrder) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.DialogColor,
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sort by",
                    color = AppTheme.colors.TextColorLight,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .selectableGroup()
            ) {
                options.forEach { (label, sortOrder) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .selectable(
                                selected = (sortOrder == selectedOption.value),
                                onClick = {
                                    selectedOption.value = sortOrder
                                    onSortOrderSelected(sortOrder)
                                    onDismiss()
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (sortOrder == selectedOption.value),
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
        },
        confirmButton = {},
        dismissButton = {}
    )
}
