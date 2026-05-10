package com.olaz.instasprite.ui.components.dialog

import androidx.compose.ui.res.stringResource
import com.olaz.instasprite.R

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.domain.model.InputField
import com.olaz.instasprite.ui.theme.AppTheme


@Composable
fun InputDialog(
    title: String,
    fields: List<InputField>,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit,
    confirmButtonText: String = "OK",
    dismissButtonText: String = "Cancel",
    extraTopContent: @Composable () -> Unit = {},
    extraBottomContent: @Composable () -> Unit = {}
) {
    val context = LocalContext.current

    val inputStates = remember {
        fields.map { field ->
            mutableStateOf(field.defaultValue)
        }
    }

    CustomDialog(
        title = title,
        onDismiss = {
            if (!isLoading) onDismiss()
        },
        onConfirm = {
            if (!isLoading) {
                val allValid = fields.withIndex().all { (i, field) ->
                    field.validator(inputStates[i].value)
                }

                if (allValid) {
                    onConfirm(inputStates.map { it.value })
                } else {
                    Toast.makeText(context, "Input errors", Toast.LENGTH_SHORT).show()
                }
            }
        },
        confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText,
        content = {
            if (isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        color = AppTheme.colors.SelectedColor,
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Column {
                    extraTopContent()

                    fields.forEachIndexed { index, field ->
                        OutlinedTextField(
                            value = inputStates[index].value,
                            onValueChange = { inputStates[index].value = it },
                            label = { Text(field.label, color = AppTheme.colors.SelectedColor) },
                            placeholder = {
                                if (field.placeholder.isNotBlank())
                                    Text(
                                        field.placeholder, color = AppTheme.colors.Subtext0Color,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                            },
                            trailingIcon = {
                                field.suffix?.let {
                                    Text(
                                        it,
                                        color = AppTheme.colors.LinkColor,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = field.keyboardType),
                            singleLine = true,
                            isError = !field.validator(inputStates[index].value),
                            supportingText = {
                                if (!field.validator(inputStates[index].value)) {
                                    Text(field.errorMessage, color = AppTheme.colors.DismissButtonColor)
                                }
                            },
                            colors = AppTheme.colors.outlineTextFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }

                    extraBottomContent()
                }
            }
        }
    )
}

@Composable
@Preview
private fun InputDialogPreview() {
    InputDialog(
        title = "Title",
        fields = listOf(
            InputField(
                label = "Label 1",
                placeholder = "Placeholder 1",
                keyboardType = KeyboardType.Text,
                suffix = "Suffix 1",
                validator = { it.isNotBlank() },
            ),
            InputField(
                label = "Label 2",
                placeholder = "Placeholder 2",
                keyboardType = KeyboardType.Number,
                validator = { it.toIntOrNull() != null },
                errorMessage = "Must be a number"
            )
        ),
        onConfirm = {},
        onDismiss = {},
        extraTopContent = {
            Text(stringResource(R.string.extra_content_at_the_top), color = Color.White)
        },
        extraBottomContent = {
            Text(stringResource(R.string.extra_content_at_the_bottom), color = Color.Gray)
        }
    )
}

@Composable
@Preview
private fun LoadingPreview() {
    InputDialog(
        title = "Title",
        fields = listOf(
            InputField(
                label = "Label 1",
                placeholder = "Placeholder 1",
                keyboardType = KeyboardType.Text,
                suffix = "Suffix 1",
                validator = { it.isNotBlank() },
            ),
            InputField(
                label = "Label 2",
                placeholder = "Placeholder 2",
                keyboardType = KeyboardType.Number,
                validator = { it.toIntOrNull() != null },
                errorMessage = "Must be a number"
            )
        ),
        onConfirm = {},
        onDismiss = {},
        extraTopContent = {
            Text(stringResource(R.string.extra_content_at_the_top), color = Color.White)
        },
        extraBottomContent = {
            Text(stringResource(R.string.extra_content_at_the_bottom), color = Color.Gray)
        },
        isLoading = true
    )
}