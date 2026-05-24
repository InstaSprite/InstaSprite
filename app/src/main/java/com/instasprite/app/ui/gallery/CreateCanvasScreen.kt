package com.instasprite.app.ui.gallery

import com.instasprite.app.utils.pixelDp

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import com.instasprite.app.data.repository.loadDefaultColorPalette
import com.instasprite.app.domain.model.ColorPalette
import com.instasprite.app.domain.model.InputField
import com.instasprite.app.ui.components.composable.TopBar
import com.instasprite.app.ui.components.composable.ColorPaletteConfig
import com.instasprite.app.ui.components.composable.PaletteListEntry
import com.instasprite.app.ui.components.composable.PalettePreview
import com.instasprite.app.ui.components.composable.InputTextField
import com.instasprite.app.ui.theme.AppTheme

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

    val nameLabel = stringResource(R.string.name)
    val untitledLabel = stringResource(R.string.untitled)
    val mustBeLessLabel = stringResource(R.string.must_be_less_than_20_chars)
    val widthLabel = stringResource(R.string.width)
    val pxLabel = stringResource(R.string.px)
    val heightLabel = stringResource(R.string.height)
    val mustBeBetweenLabel = stringResource(R.string.must_be_between_1_and_1024)
    val tapToChange = stringResource(R.string.tap_to_change)

    val fields = remember(
        nameLabel,
        untitledLabel,
        mustBeLessLabel,
        widthLabel,
        pxLabel,
        heightLabel,
        mustBeBetweenLabel
    ) {
        listOf(
            InputField(
                label = nameLabel,
                placeholder = untitledLabel,
                defaultValue = untitledLabel,
                keyboardType = KeyboardType.Text,
                validator = { it.length <= 20 },
                errorMessage = mustBeLessLabel
            ),
            InputField(
                label = widthLabel,
                placeholder = "32",
                defaultValue = "32",
                suffix = pxLabel,
                keyboardType = KeyboardType.Number,
                validator = { it.toIntOrNull() != null && it.toInt() > 0 && it.toInt() <= 1024 },
                errorMessage = mustBeBetweenLabel
            ),
            InputField(
                label = heightLabel,
                placeholder = "32",
                defaultValue = "32",
                suffix = pxLabel,
                keyboardType = KeyboardType.Number,
                validator = { it.toIntOrNull() != null && it.toInt() > 0 && it.toInt() <= 1024 },
                errorMessage = mustBeBetweenLabel
            )
        )
    }

    val inputStates = remember {
        fields.map { mutableStateOf(it.defaultValue) }
    }

    fun isInputValid() : Boolean {
        return  fields.withIndex().all { (i, field) ->
            field.validator(inputStates[i].value)
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.new_canvas),
                onBackClick = onDismiss,
                actions = {
                    Button(
                        onClick = {
                            onConfirm(
                                inputStates[0].value,
                                inputStates[1].value.toInt(),
                                inputStates[2].value.toInt()
                            )
                        },
                        enabled = isInputValid(),
                        modifier = Modifier.padding(end = 8.pixelDp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.AccentButtonColor
                        ),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            stringResource(R.string.create_canvas),
                            color = AppTheme.colors.TextColorDark
                        )
                    }
                }
            )
        },
        containerColor = AppTheme.colors.BackgroundColorDarker
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 10.pixelDp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.pixelDp))

            fields.forEachIndexed { index, field ->
                InputTextField(
                    enabled = true,
                    value = inputStates[index].value,
                    onValueChange = { inputStates[index].value = it },
                    inputField = field,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(6.pixelDp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                var palette = currentPalette

                if (palette == null) {
                    palette = ColorPalette(
                        id = -1,
                        name = "SAGE57",
                        author = "strawbrysage",
                        colors = loadDefaultColorPalette(context)
                    )
                }
                PaletteListEntry(
                    palette = palette,
                    onClick = onPaletteViewClick,
                    colorPaletteConfig = ColorPaletteConfig(isInteractive = false),
                    optionSlot = {
                        Text(
                            text = stringResource(R.string.tap_to_change),
                            fontSize = 12.sp,
                            color = AppTheme.colors.Subtext0Color
                        )
                    },
                )
            }
        }
    }
}