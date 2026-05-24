package com.instasprite.app.ui.setting

import androidx.compose.ui.res.stringResource

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import com.instasprite.app.ui.components.shape.PixelShape

import androidx.compose.foundation.verticalScroll
import com.instasprite.app.ui.components.dialog.CustomDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text

import com.instasprite.app.ui.components.composable.TopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.dialog.OtpDialog
import com.instasprite.app.ui.components.dialog.OtpEnrollmentDialog
import com.instasprite.app.ui.components.dialog.SetPasswordDialog
import com.instasprite.app.ui.setting.composable.SettingItem
import com.instasprite.app.ui.theme.AppColors
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.Catppuccin
import com.instasprite.app.ui.theme.ThemeFlavour
import com.instasprite.app.ui.theme.AppFont
import com.instasprite.app.ui.theme.buildCatppuccinTypography
import com.instasprite.app.utils.UiUtils
import com.instasprite.app.utils.pixelDp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    context: Context? = null,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val colors = AppTheme.colors

    UiUtils.SetStatusBarColor(colors.BackgroundColorDarker)
    UiUtils.SetNavigationBarColor(colors.BackgroundColorDarker)

    LaunchedEffect(uiState.setPasswordSuccess) {
        if (uiState.setPasswordSuccess) {
            Toast.makeText(context, R.string.success_settings_saved, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = context?.getString(R.string.settings) ?: "Settings",
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
                title = context?.getString(R.string.change_language) ?: "Language",
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

            if (!uiState.hasPassword) {
                SettingItem(
                    icon = R.drawable.ic_lock,
                    title = stringResource(R.string.set_password),
                    subtitle = stringResource(R.string.set_password_description),
                    onClick = { viewModel.showSetPasswordDialog() },
                    trailing = {
                        PixelIcon(
                            icon = R.drawable.ic_right_arrow,
                            contentDescription = stringResource(R.string.set_password),
                            tint = colors.Subtext0Color,
                        )
                    }
                )

                HorizontalDivider(
                    color = colors.Foreground1Color,
                    thickness = 1.pixelDp
                )
            }

            SettingItem(
                icon = R.drawable.ic_key,
                title = context.getString(R.string.enable2fa),
                subtitle = when {
                    uiState.isLoading2FAStatus -> context.getString(R.string.loading)
                    uiState.isLoadingOtp -> context.getString(R.string.loading)
                    else -> uiState.otpError ?: ""
                },
                onClick = { },
                trailing = {
                    Switch(
                        checked = uiState.is2FAEnabled,
                        onCheckedChange = { viewModel.toggle2FA(it) },
                        enabled = !uiState.isLoadingOtp && !uiState.isLoading2FAStatus,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.AccentButtonColor,
                            checkedTrackColor = colors.Foreground2Color,
                            uncheckedThumbColor = colors.Subtext0Color,
                            uncheckedTrackColor = colors.Foreground1Color
                        )
                    )
                }
            )

            HorizontalDivider(
                color = colors.Foreground1Color,
                thickness = 1.pixelDp
            )
        }

        // Language Selection Dialog
        if (uiState.showLanguageDialog) {
            CustomDialog(
                title = context?.getString(R.string.change_language) ?: "Select Language",
                onDismiss = { viewModel.dismissLanguageDialog() },
                onConfirm = { viewModel.dismissLanguageDialog() },
                confirmButtonText = context?.getString(R.string.cancel) ?: "Cancel",
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

        if (uiState.showSetPasswordDialog) {
            SetPasswordDialog(
                enabled = !uiState.isSettingPassword,
                errorText = uiState.setPasswordError,
                onDismiss = { viewModel.dismissSetPasswordDialog() },
                onConfirm = { password -> viewModel.setPassword(password) }
            )
        }

        // OTP Enrollment Dialog
        if (uiState.showOtpEnrollmentDialog) {
            OtpEnrollmentDialog(
                secret = uiState.otpSecret,
                qrCodeBase64 = uiState.otpQrCodeBase64,
                accountName = uiState.otpAccountName,
                issuer = uiState.otpIssuer,
                onDismiss = { viewModel.dismissOtpEnrollmentDialog() },
                onGotIt = { viewModel.showOtpInputDialog() }
            )
        }

        if (uiState.showOtpInputDialog) {
            OtpDialog(
                enabled = !uiState.isEnabling2FA,
                title = context.getString(R.string.enter_verification_code),
                description = uiState.enable2FAError
                    ?: context.getString(R.string.enter_6_digit_code),
                confirmButtonText = if (uiState.isEnabling2FA) context.getString(R.string.verifying) else context.getString(
                    R.string.verify
                ),
                dismissButtonText = context.getString(R.string.cancel),
                onDismiss = { viewModel.dismissOtpInputDialog() },
                onOtpComplete = { otpCode ->
                    viewModel.verifyAndEnable2FA(otpCode)
                }
            )
        }

        // Disable 2FA OTP Dialog
        if (uiState.showDisableOtpDialog) {
            OtpDialog(
                enabled = !uiState.isDisabling2FA,
                title = context.getString(R.string.disable_two_factor_authentication),
                description = uiState.disable2FAError
                    ?: context.getString(R.string.enter_6_digit_code_to_disable),
                confirmButtonText = if (uiState.isDisabling2FA) context.getString(R.string.disabling) else context.getString(
                    R.string.disable
                ),
                dismissButtonText = context.getString(R.string.cancel),
                onDismiss = { viewModel.dismissDisableOtpDialog() },
                onOtpComplete = { otpCode ->
                    viewModel.verifyAndDisable2FA(otpCode)
                }
            )
        }
    }
}

@Composable
private fun FlavourCard(
    label: String,
    previewColors: AppColors,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val borderColor = if (isSelected) colors.SelectedColor else colors.Foreground1Color

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .border(1.pixelDp, borderColor, MaterialTheme.shapes.small)
            .background(previewColors.BackgroundColor)
            .clickable(onClick = onClick)
            .padding(6.pixelDp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Swatch row — key semantic colors from the theme
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.pixelDp),
            modifier = Modifier.padding(vertical = 2.pixelDp)
        ) {
            listOf(
                previewColors.SelectedColor,
                previewColors.AccentButtonColor,
                previewColors.DismissButtonColor,
                previewColors.LinkColor,
            ).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(6.pixelDp)
                        .clip(PixelShape())
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.pixelDp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = previewColors.TextColorLight,
        )
    }
}


@Composable
private fun FontCard(
    font: AppFont,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val borderColor = if (isSelected) colors.SelectedColor else colors.Foreground1Color

    val typography = buildCatppuccinTypography(colors, font)

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .border(1.pixelDp, borderColor, MaterialTheme.shapes.small)
            .background(colors.BackgroundColorDarker)
            .clickable(onClick = onClick)
            .padding(6.pixelDp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Aa",
            style = typography.titleLarge,
            color = if (isSelected) colors.SelectedColor else colors.TextColorLight,
        )

        Spacer(modifier = Modifier.height(4.pixelDp))

        Text(
            text = font.label,
            style = typography.bodyMedium,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = colors.TextColorLight,
        )
    }
}


@Preview()
@Composable
private fun SettingScreenPreview() {

}