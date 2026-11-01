package com.instasprite.app.ui.home.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.instasprite.app.BuildConfig
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.dialog.CustomDialog
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.pixelDp

@Composable
fun HomeMenuCard(
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    CustomDialog(
        onDismiss = onDismiss,
        onConfirm = null,
        dismissButtonText = stringResource(R.string.close),
        scrollable = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.pixelDp)
        ) {
            PixelIcon(
                R.drawable.ic_launcher,
                modifier = Modifier
                    .size(64.pixelDp)
                    .border(2.pixelDp, colors.surface, MaterialTheme.shapes.small)
            )
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.pixel_art_editor_tagline),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
            Text(
                "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
            HorizontalDivider(color = colors.surfaceRaised, thickness = 1.pixelDp)
            HomeMenuAction(
                R.drawable.ic_setting,
                stringResource(R.string.settings),
                colors.purple
            ) {
                onDismiss()
                onSettingsClick()
            }
            HomeMenuAction(R.drawable.ic_info, stringResource(R.string.about), colors.blue) {
                onDismiss()
                onAboutClick()
            }
        }
    }
}

@Composable
private fun HomeMenuAction(
    @DrawableRes icon: Int,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 32.pixelDp),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.surfaceInset,
            contentColor = colors.onSurface
        )
    ) {
        PixelIcon(icon, tint = tint)
        Spacer(Modifier.width(8.pixelDp))
        Text(label, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
        PixelIcon(R.drawable.ic_right_arrow, tint = colors.textMuted)
    }
}

@Preview(name = "Home menu card")
@Composable
private fun HomeMenuCardPreview() {
    InstaSpriteTheme { HomeMenuCard({}, {}, {}) }
}
