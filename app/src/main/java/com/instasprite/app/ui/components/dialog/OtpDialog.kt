package com.instasprite.app.ui.components.dialog

import com.instasprite.app.utils.pixelDp

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.ui.components.shape.PixelShape
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun OtpDialog(
    enabled: Boolean = true,
    otpLength: Int = 6,
    title: String? = null,
    description: String? = null,
    confirmButtonText: String? = null,
    dismissButtonText: String? = null,
    onDismiss: () -> Unit,
    onOtpComplete: (String) -> Unit,
    onOtpChanged: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val defaultTitle = title ?: stringResource(R.string.enter_otp)
    val defaultConfirmText = confirmButtonText ?: stringResource(R.string.verify)
    val defaultDismissText = dismissButtonText ?: stringResource(R.string.cancel)
    val focusRequesters = remember { List(otpLength) { FocusRequester() } }
    val otpValues = remember { List(otpLength) { mutableStateOf(TextFieldValue("")) } }

    val currentOtp = otpValues.joinToString("") { it.value.text }

    LaunchedEffect(currentOtp) {
        onOtpChanged?.invoke(currentOtp)
    }

    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    CustomDialog(
        title = defaultTitle,
        onDismiss = { if (enabled) onDismiss() },
        onConfirm = { if (enabled && currentOtp.length == otpLength) onOtpComplete(currentOtp) },
        confirmButtonText = "",
        dismissButtonText = "",
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            description?.let {
                Text(
                    text = it,
                    color = AppTheme.colors.Subtext0Color,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.pixelDp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.pixelDp),
                modifier = Modifier.fillMaxWidth()
            ) {
                otpValues.forEachIndexed { index, valueState ->
                    var isFocused by remember { mutableStateOf(false) }

                    OtpTextField(
                        enabled = enabled,
                        value = valueState.value,
                        isFocused = isFocused,
                        onValueChange = { newValue ->
                            val oldText = valueState.value.text
                            val digits = newValue.text.filter { it.isDigit() }

                            // Detect paste operation (more than 1 character)
                            if (digits.length > 1) {
                                // Fill all fields with pasted digits
                                val digitsToFill = digits.take(otpLength)
                                digitsToFill.forEachIndexed { fillIndex, char ->
                                    if (fillIndex < otpLength) {
                                        otpValues[fillIndex].value = TextFieldValue(
                                            text = char.toString(),
                                            selection = TextRange(1)
                                        )
                                    }
                                }

                                // Move focus to the last filled field or the last field
                                val lastFilledIndex = (digitsToFill.length - 1).coerceAtMost(otpLength - 1)
                                focusRequesters[lastFilledIndex].requestFocus()
                            } else {
                                // Single character input (normal typing)
                                val digit = digits.takeLast(1)

                                if (digit.length <= 1) {
                                    valueState.value = TextFieldValue(
                                        text = digit,
                                        selection = TextRange(digit.length)
                                    )

                                    if (digit.isNotEmpty() && index < otpLength - 1) {
                                        focusRequesters[index + 1].requestFocus()
                                    }

                                    if (oldText.isNotEmpty() && digit.isEmpty() && index > 0) {
                                        focusRequesters[index - 1].requestFocus()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequesters[index])
                            .onFocusChanged { isFocused = it.isFocused }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.pixelDp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    enabled = enabled,
                    onClick = onDismiss
                ) {
                    Text(
                        text = defaultDismissText,
                        color = AppTheme.colors.TextColorLight
                    )
                }
                Button(
                    enabled = enabled && currentOtp.length == otpLength,
                    onClick = {
                        onOtpComplete(currentOtp)
                    },
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.AccentButtonColor,
                        disabledContainerColor = AppTheme.colors.Foreground1Color
                    ),
                ) {
                    Text(
                        text = defaultConfirmText,
                        color = if (enabled && currentOtp.length == otpLength) {
                            AppTheme.colors.TextColorDark
                        } else {
                            AppTheme.colors.TextColorLight
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OtpTextField(
    enabled: Boolean,
    value: TextFieldValue,
    isFocused: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(38.pixelDp)
            .border(
                width = 2.pixelDp,
                color = if (isFocused || value.text.isNotEmpty()) {
                    AppTheme.colors.AccentButtonColor
                } else {
                    AppTheme.colors.Foreground2Color
                },
                shape = MaterialTheme.shapes.small
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            enabled = enabled,
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxSize(),
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                color = AppTheme.colors.TextColorLight
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            cursorBrush = SolidColor(Color.Transparent)
        )
    }
}

@Preview
@Composable
private fun OtpDialogPreview() {
    val context = LocalContext.current
    OtpDialog(
        title = stringResource(R.string.enter_verification_code),
        onDismiss = {},
        onOtpComplete = {},
        onOtpChanged = {}
    )
}

