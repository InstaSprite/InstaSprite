package com.olaz.instasprite.ui.social.feed.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.R
import com.olaz.instasprite.ui.components.dialog.CustomDialog
import com.olaz.instasprite.ui.social.feed.VerifyEmailState
import com.olaz.instasprite.ui.theme.CatppuccinTypography
import com.olaz.instasprite.ui.theme.CatppuccinUI

@Composable
fun VerifyEmailDialog(
    verifyEmailState: VerifyEmailState,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    LocalContext.current

    CustomDialog(
        title = stringResource(R.string.verify_email),
        onDismiss = {
            if (!verifyEmailState.isSending) onDismiss()
        },
        onConfirm = {
            if (!verifyEmailState.isSending) onConfirm()
        },
        confirmButtonText = stringResource(R.string.verify),
        dismissButtonText = stringResource(R.string.cancel)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            if (verifyEmailState.isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = CatppuccinUI.CurrentPalette.Peach,
                    strokeWidth = 4.dp
                )

                Text(
                    text = stringResource(R.string.sending_email),
                    style = CatppuccinTypography.bodyMedium
                )
            } else {
                Text(
                    text = stringResource(R.string.verify_email_desc),
                    style = CatppuccinTypography.bodyMedium
                )
            }
        }
    }
}
