package com.instasprite.app.ui.drawing.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instasprite.app.domain.model.BlendMode
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.drawCheckerboard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayerOptionsDialog(
    layer: Layer,
    layerImage: ImageBitmap?,
    canvasWidth: Int,
    canvasHeight: Int,
    canMergeDown: Boolean,
    onBlendModeSelected: (BlendMode) -> Unit,
    onMergeDown: () -> Unit,
    onDismiss: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val blendModeOptions = BlendMode.entries
    val currentBlendMode = layer.blendMode

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.DialogColor,
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Layer Options",
                    color = AppTheme.colors.TextColorLight,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Layer Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (layerImage != null) {
                        Image(
                            bitmap = layerImage,
                            contentDescription = "Layer Preview",
                            contentScale = ContentScale.FillWidth,
                            filterQuality = FilterQuality.None,
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawCheckerboard(
                                    canvasWidth = canvasWidth,
                                    canvasHeight = canvasHeight
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Blend Mode Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = currentBlendMode.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Blend Mode") },
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
                        blendModeOptions.forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                        color = AppTheme.colors.TextColorLight
                                    )
                                },
                                onClick = {
                                    onBlendModeSelected(mode)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Merge Down Button
                if (canMergeDown) {
                    Button(
                        onClick = {
                            onMergeDown()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.SelectedColor,
                            contentColor = AppTheme.colors.TextColorDark
                        ),
                        modifier = Modifier
                    ) {
                        Text(
                            text = "Merge Down",
                            color = AppTheme.colors.TextColorDark,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = AppTheme.colors.TextColorLight
                )
            }
        }
    )
}

@Preview
@Composable
private fun PreviewLayerOptionsDialog() {
    InstaSpriteTheme {
        LayerOptionsDialog(
            layer = Layer(
                id = "1",
                name = "Test Layer",
                blendMode = BlendMode.MULTIPLY,
                tiles = emptyMap()
            ),
            layerImage = null,
            canvasWidth = 16,
            canvasHeight = 16,
            canMergeDown = true,
            onBlendModeSelected = {},
            onMergeDown = {},
            onDismiss = {}
        )
    }
}
