package com.instasprite.app.ui.social.auth

import com.instasprite.app.utils.pixelDp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instasprite.app.R
import com.instasprite.app.data.model.AccountPreferences
import com.instasprite.app.data.model.AccountType
import com.instasprite.app.data.network.GoogleAuth
import com.instasprite.app.data.network.model.GoogleLoginRequestDto
import com.instasprite.app.domain.session.SocialSessionState
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.dialog.OtpDialog
import com.instasprite.app.ui.home.SocialSessionViewModel
import com.instasprite.app.ui.social.auth.component.AuthForm
import com.instasprite.app.ui.social.auth.component.GoogleSignInButton
import com.instasprite.app.ui.social.auth.component.SavedAccountList
import com.instasprite.app.ui.social.auth.contract.AuthContentScreenState
import com.instasprite.app.ui.social.auth.contract.AuthContentState
import com.instasprite.app.ui.social.auth.contract.AuthMode
import com.instasprite.app.ui.social.auth.contract.AuthScreenEvent
import com.instasprite.app.ui.social.auth.contract.GoogleAuthUiState
import com.instasprite.app.ui.social.auth.dialog.ForgotPasswordDialog
import com.instasprite.app.ui.social.auth.dialog.SavedAccountLoginDialog
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.UiUtils

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    sessionViewModel: SocialSessionViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = {},
) {
    UiUtils.SetStatusBarColor(AppTheme.colors.BackgroundColorDarker)
    UiUtils.SetNavigationBarColor(AppTheme.colors.BackgroundColorDarker)

    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val state by viewModel.contentState.collectAsState(initial = AuthContentState())
    val sessionState by sessionViewModel.sessionState.collectAsState()

    var isGoogleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentScreenState by remember { mutableStateOf(AuthContentScreenState.AUTH_FORM) }
    var selectedAccountForLogin by remember { mutableStateOf<AccountPreferences?>(null) }

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            isGoogleLoading = false
            if (result.resultCode == Activity.RESULT_CANCELED) {
                errorMessage = context.getString(R.string.auth_cancelled_by_user)
            }
        }

    LaunchedEffect(state.accounts) {
        currentScreenState = if (state.accounts.isNotEmpty()) {
            AuthContentScreenState.ACCOUNT_LIST
        } else {
            AuthContentScreenState.AUTH_FORM
        }
    }

    LaunchedEffect(state.uiState.jwt, sessionState) {
        if (state.uiState.jwt != null && sessionState is SocialSessionState.LoggedIn) {
            isGoogleLoading = false
            onLoginSuccess()
        }
    }

    LaunchedEffect(state.uiState.errorMessage) {
        state.uiState.errorMessage?.let { err ->
            isGoogleLoading = false
            errorMessage = err
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            errorMessage = null
        }
    }

    LaunchedEffect(state.uiState.isRegisterSuccess) {
        if (state.uiState.isRegisterSuccess) {
            Toast.makeText(context, context.getString(R.string.register_succeed), Toast.LENGTH_LONG)
                .show()
            viewModel.resetRegisterSuccess()
            viewModel.switchToLogin()
        }
    }

    LaunchedEffect(state.forgotPasswordState.isPasswordResetSuccess) {
        if (state.forgotPasswordState.isPasswordResetSuccess) {
            Toast.makeText(context, "Password reset successfully", Toast.LENGTH_SHORT).show()
        }
    }

    val event = remember(viewModel, onLoginSuccess, activity, launcher, scope) {
        AuthScreenEvent(
            onLoginSuccess = onLoginSuccess,
            onDismissSavedAccountLogin = { selectedAccountForLogin = null },
            onLoginWithSavedAccount = viewModel::login,
            onDismissOtpDialog = viewModel::dismissOtpDialog,
            onVerifyOtp = viewModel::verifyOtpAndLogin,
            onDismissForgotPasswordDialog = viewModel::dismissForgotPasswordDialog,
            onForgotPassword = viewModel::forgotPassword,
            onResetPassword = viewModel::resetPassword,
            onLogin = viewModel::login,
            onRegister = viewModel::register,
            onSwitchToRegister = viewModel::switchToRegister,
            onSwitchToLogin = viewModel::switchToLogin,
            onShowForgotPasswordDialog = viewModel::showForgotPasswordDialog,
            onShowAccountList = { currentScreenState = AuthContentScreenState.ACCOUNT_LIST },
            onShowAuthForm = { currentScreenState = AuthContentScreenState.AUTH_FORM },
            onSelectAccount = { selectedAccountForLogin = it },
            onRemoveAccount = viewModel::removeAccountFromList,
            onGoogleSignInResultCancelled = { message ->
                isGoogleLoading = false
                errorMessage = message
            },
            onResetRegisterSuccess = viewModel::resetRegisterSuccess,
            onGoogleSignInClick = { hostActivity, resultLauncher, onLoginWithGoogle, onError ->
                isGoogleLoading = true
                errorMessage = null
                GoogleAuth.doGoogleSignIn(
                    context = hostActivity,
                    scope = scope,
                    launcher = resultLauncher,
                    login = { _, idToken ->
                        onLoginWithGoogle(GoogleLoginRequestDto(idToken))
                    },
                    onError = { error ->
                        isGoogleLoading = false
                        onError(error)
                    }
                )
            },
        )
    }

    AuthContent(
        modifier = modifier,
        state = state.copy(
            currentScreenState = currentScreenState,
            selectedAccountForLogin = selectedAccountForLogin,
            isGoogleLoading = isGoogleLoading,
            errorMessage = errorMessage
        ),
        event = event,
        launcher = launcher,
        activity = activity,
        onGoogleLogin = viewModel::loginWithGoogleIdToken,
    )
}

@Composable
fun AuthContent(
    modifier: Modifier = Modifier,
    state: AuthContentState,
    event: AuthScreenEvent,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null,
    activity: Activity? = null,
    onGoogleLogin: (GoogleLoginRequestDto) -> Unit = {},
) {
    LocalContext.current

    state.selectedAccountForLogin?.let { account ->
        SavedAccountLoginDialog(
            enabled = !state.isGoogleLoading,
            account = account,
            onDismiss = event.onDismissSavedAccountLogin,
            onLoginWithPassword = event.onLoginWithSavedAccount,
        )
    }

    if (state.uiState.showOtpDialog) {
        OtpDialog(
            enabled = !state.uiState.isLoading,
            title = stringResource(R.string.enter_verification_code),
            description = state.uiState.otpError ?: stringResource(R.string.enter_6_digit_code),
            confirmButtonText = if (state.uiState.isLoading) stringResource(R.string.verifying) else stringResource(
                R.string.verify
            ),
            dismissButtonText = stringResource(R.string.cancel),
            onDismiss = event.onDismissOtpDialog,
            onOtpComplete = event.onVerifyOtp,
        )
    }

    if (state.forgotPasswordState.showDialog) {
        ForgotPasswordDialog(
            forgotPasswordState = state.forgotPasswordState,
            onDismiss = event.onDismissForgotPasswordDialog,
            onForgotPassword = event.onForgotPassword,
            onResetPassword = event.onResetPassword,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.BackgroundColorDarker)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.pixelDp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PixelIcon(
                icon = R.drawable.ic_launcher,
                contentDescription = stringResource(R.string.app_logo),
                modifier = Modifier.size(66.pixelDp),
            )

            Spacer(modifier = Modifier.height(16.pixelDp))

            if (state.uiState.authMode == AuthMode.LOGIN || state.currentScreenState == AuthContentScreenState.ACCOUNT_LIST) {
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.TextColorLight,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(6.pixelDp))

                Text(
                    text = stringResource(R.string.create_pixel_art_with_ease),
                    fontSize = 16.sp,
                    color = AppTheme.colors.Foreground2Color,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.pixelDp))
            }

            AnimatedContent(
                targetState = state.currentScreenState,
                label = "AuthScreenSwitch",
            ) { screenState ->
                when (screenState) {
                    AuthContentScreenState.AUTH_FORM -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            AuthForm(
                                onLoginClick = event.onLogin,
                                onRegisterClick = event.onRegister,
                                onSwitchToRegister = event.onSwitchToRegister,
                                onSwitchToLogin = event.onSwitchToLogin,
                                onForgotPasswordClick = event.onShowForgotPasswordDialog,
                                isLoading = state.uiState.isLoading || state.isGoogleLoading,
                                uiState = state.uiState,
                            )

                            if (state.accounts.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.pixelDp))

                                Text(
                                    text = stringResource(R.string.login_with_existed),
                                    color = AppTheme.colors.TextColorLight,
                                    modifier = Modifier.clickable(onClick = event.onShowAccountList),
                                )
                            }
                        }
                    }

                    AuthContentScreenState.ACCOUNT_LIST -> {
                        SavedAccountList(
                            onAccountSelected = event.onSelectAccount,
                            onAddAccountClick = event.onShowAuthForm,
                            onRemoveAccountClick = event.onRemoveAccount,
                            accounts = state.accounts,
                        )
                    }
                }
            }

            if (state.uiState.authMode != AuthMode.REGISTER) {
                Spacer(modifier = Modifier.height(2.pixelDp))

                Text(
                    text = stringResource(R.string.or),
                    color = AppTheme.colors.TextColorLight,
                )

                Spacer(modifier = Modifier.height(4.pixelDp))

                GoogleSignInButton(
                    onClick = {
                        val hostActivity = activity ?: return@GoogleSignInButton
                        val resultLauncher = launcher ?: return@GoogleSignInButton
                        event.onGoogleSignInClick(
                            hostActivity,
                            resultLauncher,
                            onGoogleLogin
                        ) { error ->
                            event.onGoogleSignInResultCancelled(error)
                        }
                    },
                    isLoading = state.isGoogleLoading || state.uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(0.8f),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthContentFormPreview() {
    InstaSpriteTheme {
        AuthContent(
            state = AuthContentState(
                uiState = GoogleAuthUiState(authMode = AuthMode.LOGIN),
                currentScreenState = AuthContentScreenState.AUTH_FORM,
            ),
            event = previewAuthEvent(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthContentAccountListPreview() {
    InstaSpriteTheme {
        AuthContent(
            state = AuthContentState(
                accounts = listOf(
                    AccountPreferences(
                        username = "deviljho",
                        name = "John Sprite",
                        email = "john@insta.sprite",
                        avatarUrl = null,
                        accountType = AccountType.LOCAL,
                    ),
                ),
                currentScreenState = AuthContentScreenState.ACCOUNT_LIST,
            ),
            event = previewAuthEvent(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthContentRegisterPreview() {
    InstaSpriteTheme {
        AuthContent(
            state = AuthContentState(
                uiState = GoogleAuthUiState(authMode = AuthMode.REGISTER),
                currentScreenState = AuthContentScreenState.AUTH_FORM,
            ),
            event = previewAuthEvent(),
        )
    }
}

private fun previewAuthEvent() = AuthScreenEvent(
    onLoginSuccess = {},
    onDismissSavedAccountLogin = {},
    onLoginWithSavedAccount = {},
    onDismissOtpDialog = {},
    onVerifyOtp = {},
    onDismissForgotPasswordDialog = {},
    onForgotPassword = {},
    onResetPassword = { _, _ -> },
    onLogin = {},
    onRegister = {},
    onSwitchToRegister = {},
    onSwitchToLogin = {},
    onShowForgotPasswordDialog = {},
    onShowAccountList = {},
    onShowAuthForm = {},
    onSelectAccount = {},
    onRemoveAccount = {},
    onGoogleSignInResultCancelled = {},
    onResetRegisterSuccess = {},
    onGoogleSignInClick = { _, _, _, _ -> },
)
