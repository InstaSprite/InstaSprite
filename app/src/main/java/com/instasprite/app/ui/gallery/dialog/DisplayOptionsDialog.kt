package com.instasprite.app.ui.gallery.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.ui.gallery.GalleryLayoutMode
import com.instasprite.app.ui.gallery.SpriteListOrder
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun DisplayOptionsDialog(
    onSortOrderSelected: (SpriteListOrder) -> Unit,
    spriteListOrder: SpriteListOrder,
    onLayoutModeSelected: (GalleryLayoutMode) -> Unit,
    layoutMode: GalleryLayoutMode,
    onDismiss: () -> Unit,
) {
    var selectedSortOrder by remember { mutableStateOf(spriteListOrder) }
    var selectedLayoutMode by remember { mutableStateOf(layoutMode) }

    val sortOptions = mapOf(
        stringResource(R.string.sort_a_z) to SpriteListOrder.Name,
        stringResource(R.string.sort_z_a) to SpriteListOrder.NameDesc,
        stringResource(R.string.sort_date_created) to SpriteListOrder.DateCreated,
        stringResource(R.string.sort_date_created_desc) to SpriteListOrder.DateCreatedDesc,
        stringResource(R.string.sort_date_modified) to SpriteListOrder.LastModified,
        stringResource(R.string.sort_date_modified_desc) to SpriteListOrder.LastModifiedDesc
    )
    val currentSortLabel = sortOptions.entries.firstOrNull { it.value == selectedSortOrder }?.key ?: ""

    val layoutOptions = mapOf(
        stringResource(R.string.layout_list) to GalleryLayoutMode.List,
        stringResource(R.string.layout_staggered) to GalleryLayoutMode.StaggeredGrid,
        stringResource(R.string.layout_square_grid) to GalleryLayoutMode.SquareGrid
    )
    val currentLayoutLabel = layoutOptions.entries.firstOrNull { it.value == selectedLayoutMode }?.key ?: ""

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.DialogColor,
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.display_options),
                    color = AppTheme.colors.TextColorLight,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                DropdownField(
                    label = stringResource(R.string.sort_by),
                    options = sortOptions.keys.toList(),
                    selectedOption = currentSortLabel,
                    onOptionSelected = { label ->
                        sortOptions[label]?.let { selectedSortOrder = it }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                DropdownField(
                    label = stringResource(R.string.layout_mode),
                    options = layoutOptions.keys.toList(),
                    selectedOption = currentLayoutLabel,
                    onOptionSelected = { label ->
                        layoutOptions[label]?.let { selectedLayoutMode = it }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSortOrderSelected(selectedSortOrder)
                    onLayoutModeSelected(selectedLayoutMode)
                    onDismiss()
                }
            ) {
                Text(
                    text = "Apply",
                    color = AppTheme.colors.AccentButtonColor
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = AppTheme.colors.TextColorLight
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = AppTheme.colors.outlineTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = AppTheme.colors.DropDownMenuColor
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = AppTheme.colors.TextColorLight) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
