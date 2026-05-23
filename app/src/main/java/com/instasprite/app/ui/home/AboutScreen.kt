package com.instasprite.app.ui.home

import com.instasprite.app.utils.pixelDp

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.BuildConfig
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.Catppuccin
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.UiUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit = {}
) {
    val colors = AppTheme.colors
    val context = LocalContext.current

    UiUtils.SetStatusBarColor(colors.BackgroundColorDarker)
    UiUtils.SetNavigationBarColor(colors.BackgroundColorDarker)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        PixelIcon(
                            icon = R.drawable.ic_left_arrow,
                            contentDescription = stringResource(R.string.back),
                            tint = AppTheme.colors.DismissButtonColor
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.about),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colors.TextColorLight
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.TopBarColor,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.BackgroundColor)
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
                color = colors.TextColorLight
            )

            // Version
            Text(
                text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                fontSize = 14.sp,
                color = colors.Subtext0Color
            )

            Spacer(modifier = Modifier.height(6.pixelDp))

            // Tagline
            Text(
                text = stringResource(R.string.pixel_art_creation_social_sharing),
                fontSize = 14.sp,
                color = colors.Subtext1Color,
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
                    iconTint = colors.InfoColor,
                    label = "Developer",
                    value = "pBuoc"
                )

                HorizontalDivider(color = colors.Foreground1Color, thickness = 1.pixelDp)

                AboutInfoCard(
                    icon = R.drawable.ic_palette,
                    iconTint = colors.SelectedColor,
                    label = "Theme",
                    value = "Catppuccin"
                )

                HorizontalDivider(color = colors.Foreground1Color, thickness = 1.pixelDp)

                AboutInfoCard(
                    icon = R.drawable.ic_edit,
                    iconTint = colors.AccentButtonColor,
                    label = "Built with",
                    value = "Jetpack Compose"
                )
            }
            Spacer(modifier = Modifier.height(22.pixelDp))

            // Footer
            Text(
                text = stringResource(R.string.made_with_3_and_lots_of_pixels),
                fontSize = 12.sp,
                color = colors.Subtext0Color,
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
            color = colors.Subtext0Color,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colors.TextColorLight
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
            color = colors.TextColorLight,
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