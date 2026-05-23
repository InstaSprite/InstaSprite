package com.instasprite.app.ui.components.dialog

import com.instasprite.app.utils.pixelDp

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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.instasprite.app.R
import com.instasprite.app.ui.theme.AppTheme

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

    CustomDialog(
        title = context.getString(R.string.enable_two_factor_authentication),
        onDismiss = onDismiss,
        onConfirm = onGotIt,
        confirmButtonText = context.getString(R.string.got_it),
        dismissButtonText = context.getString(R.string.cancel),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.pixelDp)
        ) {
            Text(
                text = context.getString(R.string.scan_qr_code_instruction),
                color = AppTheme.colors.TextColorLight,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap,
                    contentDescription = context.getString(R.string.qr_code_for_otp),
                    modifier = Modifier.size(166.pixelDp)
                )
            } else {
                Text(
                    text = context.getString(R.string.failed_to_load_qr_code),
                    color = AppTheme.colors.DismissButtonColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = context.getString(R.string.or_enter_secret_manually),
                color = AppTheme.colors.Subtext0Color,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            SelectionContainer {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.pixelDp, vertical = 6.pixelDp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = secret,
                        color = AppTheme.colors.AccentButtonColor,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.pixelDp))

            Text(
                text = context.getString(R.string.account_issuer_info, accountName, issuer),
                color = AppTheme.colors.Subtext0Color,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.pixelDp))

            Text(
                text = context.getString(R.string.after_scanning_instruction),
                color = AppTheme.colors.TextColorLight,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
        }
    }
}
