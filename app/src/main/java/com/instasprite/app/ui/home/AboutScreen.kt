package com.instasprite.app.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.instasprite.app.BuildConfig
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.composable.TopBar
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.Catppuccin
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.UiUtils
import com.instasprite.app.utils.pixelDp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit = {}
) {
    val colors = AppTheme.colors
    val context = LocalContext.current

    UiUtils.SetStatusBarColor(colors.surfaceInset)
    UiUtils.SetNavigationBarColor(colors.surfaceInset)

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.about),
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(22.pixelDp))

            // App Icon
            PixelIcon(
                icon = R.drawable.ic_launcher,
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(58.pixelDp)
            )

            Spacer(modifier = Modifier.height(10.pixelDp))

            // App Name
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )

            // Version
            Text(
                text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                fontSize = 14.sp,
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(6.pixelDp))

            // Tagline
            Text(
                text = stringResource(R.string.pixel_art_editor_tagline),
                fontSize = 14.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.pixelDp)
            )

            Spacer(modifier = Modifier.height(16.pixelDp))

            // Decos
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.pixelDp),
                modifier = Modifier.padding(horizontal = 32.pixelDp)
            ) {
                val palette = Catppuccin.Mocha
                listOf(
                    palette.Red, palette.Peach, palette.Yellow, palette.Green,
                    palette.Teal, palette.Blue, palette.Mauve, palette.Pink,
                    palette.Flamingo, palette.Rosewater
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .size(4.pixelDp)
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.pixelDp))

            // Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.pixelDp),
                verticalArrangement = Arrangement.spacedBy(2.pixelDp)
            ) {
                AboutInfoCard(
                    icon = R.drawable.ic_profile,
                    iconTint = colors.lightPink,
                    label = "Developer",
                    value = "pBuoc"
                )

                HorizontalDivider(color = colors.surfaceRaised, thickness = 1.pixelDp)

                AboutInfoCard(
                    icon = R.drawable.ic_palette,
                    iconTint = colors.purple,
                    label = "Theme",
                    value = "Catppuccin"
                )

                HorizontalDivider(color = colors.surfaceRaised, thickness = 1.pixelDp)

                AboutInfoCard(
                    icon = R.drawable.ic_edit,
                    iconTint = colors.green,
                    label = "Built with",
                    value = "Jetpack Compose"
                )
            }
            Spacer(modifier = Modifier.height(22.pixelDp))

            // Footer
            Text(
                text = stringResource(R.string.made_with_3_and_lots_of_pixels),
                fontSize = 12.sp,
                color = colors.textMuted,
            )

            Spacer(modifier = Modifier.height(16.pixelDp))
        }
    }
}

@Composable
private fun AboutInfoCard(
    @DrawableRes icon: Int,
    iconTint: Color,
    label: String,
    value: String
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.pixelDp, horizontal = 2.pixelDp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PixelIcon(
            icon = icon,
            contentDescription = label,
            tint = iconTint,
        )

        Spacer(modifier = Modifier.width(10.pixelDp))

        Text(
            text = label,
            fontSize = 14.sp,
            color = colors.textMuted,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface
        )
    }
}

@Composable
private fun AboutLinkCard(
    label: String,
    url: String,
    linkColor: Color,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.pixelDp, horizontal = 2.pixelDp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = colors.onSurface,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = url,
            fontSize = 13.sp,
            color = linkColor
        )
    }
}


@Preview
@Composable
private fun AboutScreenPreview() {
    InstaSpriteTheme {
        AboutScreen()
    }
}