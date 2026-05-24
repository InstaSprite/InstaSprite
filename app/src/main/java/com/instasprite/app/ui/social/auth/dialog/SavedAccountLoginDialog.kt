package com.instasprite.app.ui.social.auth.dialog

import com.instasprite.app.utils.pixelDp

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.instasprite.app.R
import com.instasprite.app.data.model.AccountPreferences
import com.instasprite.app.data.model.AccountType
import com.instasprite.app.domain.model.InputField
import com.instasprite.app.domain.model.LoginRequest
import com.instasprite.app.ui.components.composable.InputTextField
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.shape.PixelShape
import com.instasprite.app.ui.social.feed.component.ProfileImage
import com.instasprite.app.ui.theme.AppTheme

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun SavedAccountLoginDialog(
    enabled: Boolean = true,
    account: AccountPreferences,
    onDismiss: () -> Unit,
    onLoginWithPassword: (LoginRequest) -> Unit
) {
    val context = LocalContext.current

    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val visualTransformation =
        if (isPasswordVisible) VisualTransformation.None
        else PasswordVisualTransformation('*')

    val passwordInputTextField = remember {
        InputField(
            label = context.getString(R.string.password),
            validator = { password ->
                password.length >= 6 && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() }
            },
            keyboardType = KeyboardType.Password,
            errorMessage = context.getString(R.string.password_validation_error)
        )
    }

    Dialog(
        onDismissRequest = {
            if (enabled) onDismiss()
        }
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.BackgroundColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.pixelDp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileImage(
                    imageUrl = account.avatarUrl,
                    size = 48.pixelDp
                )

                Spacer(modifier = Modifier.height(8.pixelDp))

                Text(
                    text = stringResource(R.string.welcome_back) + ", ${account.name ?: account.username}",
                    color = AppTheme.colors.TextColorLight,
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    text = account.username,
                    fontSize = 14.sp,
                    color = AppTheme.colors.Subtext0Color,
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(16.pixelDp))

                InputTextField(
                    enabled = enabled,
                    value = password,
                    onValueChange = { v ->
                        password = v
                    },
                    imeAction = ImeAction.Done,
                    inputField = passwordInputTextField,
                    trailingIcon = {
                        IconToggleButton(
                            checked = isPasswordVisible,
                            colors = IconButtonDefaults.iconToggleButtonColors().copy(
                                checkedContentColor = AppTheme.colors.LinkColor,
                                contentColor = AppTheme.colors.InactiveColor,
                            ),
                            onCheckedChange = { isPasswordVisible = it },
                        ) {
                            PixelIcon(
                                icon = if (isPasswordVisible)
                                    R.drawable.ic_visible_on
                                else
                                R.drawable.ic_visible_off,
                                tint = AppTheme.colors.TextColorLight,
                            )
                        }
                    },
                    visualTransformation = visualTransformation
                )

                Spacer(modifier = Modifier.height(10.pixelDp))

                Button(
                    enabled = enabled
                            && passwordInputTextField.validator(password),
                    onClick = {
                        onLoginWithPassword(
                            LoginRequest(
                                identifier = account.email ?: account.username,
                                password = password
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.SelectedColor,
                        contentColor = AppTheme.colors.TextColorLight,
                        disabledContainerColor = AppTheme.colors.Foreground1Color,
                    ),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(38.pixelDp)
                ) {
                    Text(
                        text = stringResource(R.string.login),
                        color = if (enabled) AppTheme.colors.TextColorDark else AppTheme.colors.TextColorLight,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(locale = "vi")
@Composable
private fun Preview() {
    SavedAccountLoginDialog(
        account = AccountPreferences(
            name = "John Doe",
            username = "alsdfsd alo",
            email = "alo@asd",
            avatarUrl = "dummy",
            accountType = AccountType.LOCAL
        ),
        onDismiss = {},
        onLoginWithPassword = {}
    )
}