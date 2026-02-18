package com.olaz.instasprite.ui.gallery

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.data.repository.loadDefaultColorPalette
import com.olaz.instasprite.domain.model.ColorPalette
import com.olaz.instasprite.domain.model.InputField
import com.olaz.instasprite.ui.components.composable.BackButton
import com.olaz.instasprite.ui.components.composable.ColorPaletteConfig
import com.olaz.instasprite.ui.components.composable.ColorPaletteView
import com.olaz.instasprite.ui.components.composable.TopBar
import com.olaz.instasprite.ui.theme.CatppuccinTypography
import com.olaz.instasprite.ui.theme.CatppuccinUI

@Composable
fun CreateCanvasScreen(
    onDismiss: () -> Unit,
    onConfirm: (name: String, width: Int, height: Int) -> Unit,
    onPaletteViewClick: () -> Unit = {},
    selectedPalette: ColorPalette? = null,
) {
    BackHandler(onBack = onDismiss)

    val context = LocalContext.current

    var currentPalette by rememberSaveable { mutableStateOf(selectedPalette) }
    LaunchedEffect(selectedPalette) {
        if (selectedPalette != null) currentPalette = selectedPalette
    }

    val fields = remember {
        listOf(
            InputField(
                label = "Name",
                placeholder = "Untitled",
                defaultValue = "Untitled",
                keyboardType = KeyboardType.Text,
                validator = { it.length <= 20 },
                errorMessage = "Must be less than 20 characters"
            ),
            InputField(
                label = "Width",
                placeholder = "16",
                defaultValue = "16",
                suffix = "px",
                keyboardType = KeyboardType.Number,
                validator = { it.toIntOrNull() != null && it.toIntOrNull() != 0 },
                errorMessage = "Must be a number greater than 0"
            ),
            InputField(
                label = "Height",
                placeholder = "16",
                defaultValue = "16",
                suffix = "px",
                keyboardType = KeyboardType.Number,
                validator = { it.toIntOrNull() != null && it.toIntOrNull() != 0 },
                errorMessage = "Must be a number greater than 0"
            )
        )
    }

    val inputStates = remember {
        fields.map { mutableStateOf(it.defaultValue) }
    }

    fun tryConfirm() {
        val allValid = fields.withIndex().all { (i, field) ->
            field.validator(inputStates[i].value)
        }
        if (allValid) {
            onConfirm(
                inputStates[0].value,
                inputStates[1].value.toInt(),
                inputStates[2].value.toInt()
            )
        } else {
            Toast.makeText(context, "Input errors", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                leftSlot = {
                    BackButton(onClick = onDismiss)
                },
                middleSlot = {
                    Text(
                        text = "New canvas",
                        color = CatppuccinUI.TextColorLight,
                        style = CatppuccinTypography.titleMedium
                    )
                },
                rightSlot = {
                    Button(
                        onClick = ::tryConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CatppuccinUI.AccentButtonColor
                        )
                    ) {
                        Text("Create", color = CatppuccinUI.TextColorDark)
                    }
                }
            )
        },
        containerColor = CatppuccinUI.BackgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            fields.forEachIndexed { index, field ->
                OutlinedTextField(
                    value = inputStates[index].value,
                    onValueChange = { inputStates[index].value = it },
                    label = { Text(field.label, color = CatppuccinUI.SelectedColor) },
                    placeholder = {
                        if (field.placeholder.isNotBlank())
                            Text(
                                field.placeholder,
                                color = CatppuccinUI.Subtext0Color,
                                style = CatppuccinTypography.bodyMedium
                            )
                    },
                    trailingIcon = {
                        field.suffix?.let {
                            Text(
                                it,
                                color = CatppuccinUI.CurrentPalette.Blue,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = field.keyboardType),
                    singleLine = true,
                    isError = !field.validator(inputStates[index].value),
                    supportingText = {
                        if (!field.validator(inputStates[index].value)) {
                            Text(field.errorMessage, color = CatppuccinUI.CurrentPalette.Red)
                        }
                    },
                    colors = CatppuccinUI.OutlineTextFieldColors.colors(),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = CatppuccinTypography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val palette = currentPalette?.colors ?: loadDefaultColorPalette(context)
                ColorPaletteView(
                    colors = palette,
                    config = ColorPaletteConfig(isInteractive = false),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onPaletteViewClick)
                )
            }
        }
    }
}
