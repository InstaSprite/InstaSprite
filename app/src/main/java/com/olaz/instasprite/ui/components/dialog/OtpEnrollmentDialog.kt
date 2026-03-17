package com.olaz.instasprite.ui.components.dialog

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olaz.instasprite.R
import com.olaz.instasprite.ui.theme.CatppuccinUI

@Composable
fun OtpEnrollmentDialog(
    secret: String,
    qrCodeBase64: String,
    accountName: String,
    issuer: String,
    onDismiss: () -> Unit,
    onGotIt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val qrBitmap = remember(qrCodeBase64) {
        try {
            val decodedBytes = Base64.decode(qrCodeBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CatppuccinUI.DialogColor,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = context.getString(R.string.enable_two_factor_authentication),
                color = CatppuccinUI.TextColorLight,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.scan_qr_code_instruction),
                    color = CatppuccinUI.TextColorLight,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = context.getString(R.string.qr_code_for_otp),
                        modifier = Modifier.size(250.dp)
                    )
                } else {
                    Text(
                        text = context.getString(R.string.failed_to_load_qr_code),
                        color = CatppuccinUI.DismissButtonColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = context.getString(R.string.or_enter_secret_manually),
                    color = CatppuccinUI.Subtext0Color,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )

                SelectionContainer {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = secret,
                            color = CatppuccinUI.AccentButtonColor,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = context.getString(R.string.account_issuer_info, accountName, issuer),
                    color = CatppuccinUI.Subtext0Color,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = context.getString(R.string.after_scanning_instruction),
                    color = CatppuccinUI.TextColorLight,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onGotIt,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CatppuccinUI.AccentButtonColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = context.getString(R.string.got_it),
                    color = CatppuccinUI.TextColorDark
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = context.getString(R.string.cancel),
                    color = CatppuccinUI.DismissButtonColor
                )
            }
        }
    )
}

