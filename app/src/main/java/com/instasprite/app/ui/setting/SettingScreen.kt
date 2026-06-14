package com.instasprite.app.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.FlavourCard
import com.instasprite.app.ui.components.composable.FontCard
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.composable.TopBar
import com.instasprite.app.ui.components.dialog.CustomDialog
import com.instasprite.app.ui.setting.composable.SettingItem
import com.instasprite.app.ui.theme.AppFont
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.Catppuccin
import com.instasprite.app.ui.theme.ThemeFlavour
import com.instasprite.app.utils.UiUtils
import com.instasprite.app.utils.pixelDp

@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = AppTheme.colors

    UiUtils.SetStatusBarColor(colors.BackgroundColorDarker)
    UiUtils.SetNavigationBarColor(colors.BackgroundColorDarker)

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.settings),
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.BackgroundColor)
                .verticalScroll(rememberScrollState())
        ) {
            // Font Selection Section
            Text(
                text = "Font",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = colors.Subtext0Color,
                modifier = Modifier.padding(
                    start = 10.pixelDp,
                    top = 10.pixelDp,
                    bottom = 6.pixelDp
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.pixelDp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AppFont.entries.forEach { font ->
                    val isSelected = uiState.appFont == font
                    FontCard(
                        font = font,
                        isSelected = isSelected,
                        onClick = { viewModel.setAppFont(font) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.pixelDp)
                            .height(64.pixelDp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.pixelDp))

            HorizontalDivider(
                color = colors.Foreground1Color,
                thickness = 1.pixelDp
            )

            // Theme Flavour Section
            Text(
                text = stringResource(R.string.theme),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = colors.Subtext0Color,
                modifier = Modifier.padding(
                    start = 10.pixelDp,
                    top = 10.pixelDp,
                    bottom = 6.pixelDp
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.pixelDp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ThemeFlavour.entries.forEach { flavour ->
                    val previewColors = Catppuccin.toAppColors(Catppuccin.fromFlavour(flavour))
                    val isSelected = uiState.themeFlavour == flavour
                    FlavourCard(
                        label = flavour.label,
                        previewColors = previewColors,
                        isSelected = isSelected,
                        onClick = { viewModel.setThemeFlavour(flavour) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.pixelDp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.pixelDp))

            HorizontalDivider(
                color = colors.Foreground1Color,
                thickness = 1.pixelDp
            )

            // Language Setting
            SettingItem(
                icon = R.drawable.ic_info,
                title = stringResource(R.string.change_language),
                subtitle = uiState.selectedLanguage,
                onClick = { viewModel.showLanguageDialog() },
                trailing = {
                    PixelIcon(
                        icon = R.drawable.ic_right_arrow,
                        contentDescription = stringResource(R.string.select_language),
                        tint = colors.Subtext0Color,
                    )
                }
            )

            HorizontalDivider(
                color = colors.Foreground1Color,
                thickness = 1.pixelDp
            )

            // Language Selection Dialog
            if (uiState.showLanguageDialog) {
                CustomDialog(
                    title = stringResource(R.string.change_language),
                    onDismiss = { viewModel.dismissLanguageDialog() },
                    onConfirm = { viewModel.dismissLanguageDialog() },
                    confirmButtonText = stringResource(R.string.cancel),
                    dismissButtonText = "",
                ) {
                    Column {
                        uiState.languages.forEachIndexed { index, language ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectLanguage(index)
                                    }
                                    .padding(vertical = 8.pixelDp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.selectedLanguage == language,
                                    onClick = {
                                        viewModel.selectLanguage(index)
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colors.SelectedColor,
                                        unselectedColor = colors.Subtext0Color
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.pixelDp))
                                Text(
                                    text = language,
                                    color = colors.TextColorLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}


@Preview()
@Composable
private fun SettingScreenPreview() {

}
