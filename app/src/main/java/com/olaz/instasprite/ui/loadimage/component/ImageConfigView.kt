package com.olaz.instasprite.ui.loadimage.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.data.repository.loadDefaultColorPalette
import com.olaz.instasprite.ui.components.composable.ColorPaletteConfig
import com.olaz.instasprite.ui.components.composable.ColorPaletteView
import com.olaz.instasprite.ui.components.composable.NumberStepper
import com.olaz.instasprite.ui.loadimage.LoadImageScreenEvent
import com.olaz.instasprite.ui.loadimage.contract.ImageConfigEvent
import com.olaz.instasprite.ui.loadimage.contract.LoadImageUiState
import com.olaz.instasprite.ui.theme.CatppuccinTypography
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme


@Composable
fun ImageConfigView(
    uiState: LoadImageUiState,
    event: LoadImageScreenEvent,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Basic", "Advanced")

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        PrimaryTabRow(
            selectedTabIndex = uiState.selectedTabIndex,
            containerColor = CatppuccinUI.BackgroundColor,
            contentColor = CatppuccinUI.TextColorLight
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedTabIndex == index,
                    onClick = { event.onConfigEvent(ImageConfigEvent.TabSelectionChange(index)) },
                    text = {
                        Text(
                            title,
                            color = CatppuccinUI.TextColorLight,
                            style = CatppuccinTypography.bodyMedium
                        )
                    },
                    selectedContentColor = CatppuccinUI.AccentButtonColor,
                    unselectedContentColor = CatppuccinUI.TextColorLight
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.selectedTabIndex == 0) {
            BasicSettingTab(uiState, event)
        } else {
            AdvancedSettingTab(uiState, event)
        }

    }
}

@Composable
private fun BasicSettingTab(
    uiState: LoadImageUiState,
    event: LoadImageScreenEvent
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NumberStepper(
            value = uiState.config.targetWidth,
            onValueChange = { event.onConfigEvent(ImageConfigEvent.TargetWidthChange(it)) },
            label = "Target Width (px)",
            changeAmount = 16,
            range = 1..512
        )

        Spacer(modifier = Modifier.height(12.dp))

        NumberStepper(
            value = uiState.config.colorCount,
            onValueChange = { event.onConfigEvent(ImageConfigEvent.ColorCountChange(it)) },
            label = "Color Count",
            range = 2..256
        )
    }
}

@Composable
private fun AdvancedSettingTab(
    uiState: LoadImageUiState,
    event: LoadImageScreenEvent
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Enable Dithering",
                color = CatppuccinUI.TextColorLight,
                style = CatppuccinTypography.bodyMedium
            )
            Switch(
                checked = uiState.config.enableDithering,
                onCheckedChange = { event.onConfigEvent(ImageConfigEvent.DitheringChange(it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CatppuccinUI.TextColorDark,
                    checkedTrackColor = CatppuccinUI.AccentButtonColor
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Apply Custom Palette",
                color = CatppuccinUI.TextColorLight,
                style = CatppuccinTypography.bodyMedium
            )
            Switch(
                checked = uiState.applyPalette,
                onCheckedChange = { event.onConfigEvent(ImageConfigEvent.ApplyPaletteChange(it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CatppuccinUI.TextColorDark,
                    checkedTrackColor = CatppuccinUI.AccentButtonColor
                )
            )
        }

        AnimatedVisibility(visible = uiState.applyPalette) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                val context = LocalContext.current
                val paletteToUse =
                    uiState.selectedPalette?.colors ?: loadDefaultColorPalette(context)
                ColorPaletteView(
                    colors = paletteToUse,
                    config = ColorPaletteConfig(isInteractive = false),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = event.onPaletteViewClick)
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewImageConfigView() {
    InstaSpriteTheme {
        ImageConfigView(
            uiState = LoadImageUiState(
                sourceBitmap = null,
                processedBitmap = null,
                isLoading = false
            ),
            event = LoadImageScreenEvent(
                onConfigEvent = {},
                onDismiss = {},
                onConfirm = { TODO() },
                onLaunchImagePicker = { },
                onPaletteViewClick = { }
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewBasicTab() {
    InstaSpriteTheme {
        BasicSettingTab(
            uiState = LoadImageUiState(
                sourceBitmap = null,
                processedBitmap = null,
                isLoading = false
            ),
            event = LoadImageScreenEvent(
                onConfigEvent = {},
                onDismiss = {},
                onConfirm = { TODO() },
                onLaunchImagePicker = { },
                onPaletteViewClick = { }
            )
        )
    }
}

@Preview
@Composable
private fun PreviewAdvancedTab() {
    InstaSpriteTheme {
        AdvancedSettingTab(
            uiState = LoadImageUiState(
                sourceBitmap = null,
                processedBitmap = null,
                isLoading = false
            ),
            event = LoadImageScreenEvent(
                onConfigEvent = {},
                onDismiss = {},
                onConfirm = { TODO() },
                onLaunchImagePicker = { },
                onPaletteViewClick = { }
            )
        )
    }
}
