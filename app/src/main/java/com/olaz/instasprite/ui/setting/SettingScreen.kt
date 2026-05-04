package com.olaz.instasprite.ui.setting

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.olaz.instasprite.R
import com.olaz.instasprite.ui.components.dialog.OtpDialog
import com.olaz.instasprite.ui.components.dialog.OtpEnrollmentDialog
import com.olaz.instasprite.ui.setting.composable.SettingItem
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.utils.UiUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    context: Context? = null,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    UiUtils.SetStatusBarColor(CatppuccinUI.BackgroundColorDarker)
    UiUtils.SetNavigationBarColor(CatppuccinUI.BackgroundColorDarker)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CatppuccinUI.TextColorLight
                        )
                    }
                },
                title = {
                    Text(
                        text = context?.getString(R.string.settings) ?: "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = CatppuccinUI.TextColorLight
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CatppuccinUI.TopBarColor,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(CatppuccinUI.BackgroundColor)
                .verticalScroll(rememberScrollState())
        ) {
            // Dark Theme Setting
            SettingItem(
                icon = Icons.Default.Warning,
                title = context?.getString(R.string.dark_mode) ?: context.getString(R.string.dark_mode),
                subtitle = context?.getString(R.string.dark_mode) ?: context.getString(R.string.dark_mode),
                trailing = {
                    Switch(
                        checked = uiState.isDarkThemeEnabled,
                        onCheckedChange = { viewModel.toggleDarkTheme(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CatppuccinUI.AccentButtonColor,
                            checkedTrackColor = CatppuccinUI.Foreground2Color,
                            uncheckedThumbColor = CatppuccinUI.Subtext0Color,
                            uncheckedTrackColor = CatppuccinUI.Foreground1Color
                        )
                    )
                }
            )

            HorizontalDivider(
                color = CatppuccinUI.Foreground1Color,
                thickness = 1.dp
            )

            // Language Setting
            SettingItem(
                icon = Icons.Default.Info,
                title = context?.getString(R.string.change_language) ?: "Language",
                subtitle = uiState.selectedLanguage,
                onClick = { viewModel.showLanguageDialog() },
                trailing = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Select Language",
                        tint = CatppuccinUI.Subtext0Color,
                        modifier = Modifier.graphicsLayer(rotationZ = 180f)
                    )
                }
            )

            HorizontalDivider(
                color = CatppuccinUI.Foreground1Color,
                thickness = 1.dp
            )

            SettingItem(
                icon = Icons.Default.Key,
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
                            checkedThumbColor = CatppuccinUI.AccentButtonColor,
                            checkedTrackColor = CatppuccinUI.Foreground2Color,
                            uncheckedThumbColor = CatppuccinUI.Subtext0Color,
                            uncheckedTrackColor = CatppuccinUI.Foreground1Color
                        )
                    )
                }
            )

            HorizontalDivider(
                color = CatppuccinUI.Foreground1Color,
                thickness = 1.dp
            )
        }

        // Language Selection Dialog
        if (uiState.showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissLanguageDialog() },
                title = {
                    Text(
                        text = context?.getString(R.string.change_language) ?: "Select Language",
                        fontWeight = FontWeight.Bold,
                        color = CatppuccinUI.TextColorLight
                    )
                },
                text = {
                    Column {
                        uiState.languages.forEachIndexed { index, language ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectLanguage(index)
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.selectedLanguage == language,
                                    onClick = {
                                        viewModel.selectLanguage(index)
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = CatppuccinUI.SelectedColor,
                                        unselectedColor = CatppuccinUI.Subtext0Color
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = language,
                                    color = CatppuccinUI.TextColorLight
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.dismissLanguageDialog() }
                    ) {
                        Text(
                            text = context?.getString(R.string.cancel) ?: "Cancel",
                            color = CatppuccinUI.DismissButtonColor
                        )
                    }
                },
                containerColor = CatppuccinUI.DialogColor
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
                description = uiState.enable2FAError ?: context.getString(R.string.enter_6_digit_code),
                confirmButtonText = if (uiState.isEnabling2FA) context.getString(R.string.verifying) else context.getString(R.string.verify),
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
                description = uiState.disable2FAError ?: context.getString(R.string.enter_6_digit_code_to_disable),
                confirmButtonText = if (uiState.isDisabling2FA) context.getString(R.string.disabling) else context.getString(R.string.disable),
                dismissButtonText = context.getString(R.string.cancel),
                onDismiss = { viewModel.dismissDisableOtpDialog() },
                onOtpComplete = { otpCode ->
                    viewModel.verifyAndDisable2FA(otpCode)
                }
            )
        }
    }
}


@Preview()
@Composable
private fun SettingScreenPreview() {

}