package com.olaz.instasprite.ui.social.auth.dialog

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.olaz.instasprite.R
import com.olaz.instasprite.data.model.AccountPreferences
import com.olaz.instasprite.data.model.AccountType
import com.olaz.instasprite.data.model.InputField
import com.olaz.instasprite.domain.model.LoginRequest
import com.olaz.instasprite.ui.components.composable.AsyncImageView
import com.olaz.instasprite.ui.components.composable.InputTextField
import com.olaz.instasprite.ui.theme.CatppuccinTypography
import com.olaz.instasprite.ui.theme.CatppuccinUI

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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CatppuccinUI.BackgroundColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImageView(
                    imageUrl = account.avatarUrl ?: "",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(CatppuccinUI.Foreground0Color)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.welcome_back) + ", ${account.name ?: account.username}",
                    color = CatppuccinUI.TextColorLight,
                    style = CatppuccinTypography.titleMedium,
                )

                Text(
                    text = account.username,
                    fontSize = 14.sp,
                    color = CatppuccinUI.Subtext0Color,
                    style = CatppuccinTypography.labelMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                                checkedContentColor = CatppuccinUI.CurrentPalette.Blue,
                                contentColor = CatppuccinUI.CurrentPalette.Overlay0,
                            ),
                            onCheckedChange = { isPasswordVisible = it },
                        ) {
                            if (isPasswordVisible) Icon(Icons.Default.Visibility, null)
                            else Icon(Icons.Default.VisibilityOff, null)
                        }
                    },
                    visualTransformation = visualTransformation
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                        containerColor = CatppuccinUI.SelectedColor,
                        contentColor = CatppuccinUI.TextColorLight,
                        disabledContainerColor = CatppuccinUI.Foreground1Color,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.login),
                        color = if (enabled) CatppuccinUI.TextColorDark else CatppuccinUI.TextColorLight,
                        style = CatppuccinTypography.bodyMedium
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