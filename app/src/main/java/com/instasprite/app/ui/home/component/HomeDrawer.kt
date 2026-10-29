package com.instasprite.app.ui.home.component

import androidx.annotation.DrawableRes
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.pixelDp

@Composable
fun HomeDrawer(
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val drawerItemColors = NavigationDrawerItemDefaults.colors(
        unselectedContainerColor = Color.Transparent,
        selectedContainerColor = AppTheme.colors.Foreground0Color,
        unselectedIconColor = AppTheme.colors.TextColorLight,
        selectedIconColor = AppTheme.colors.TextColorLight,
        unselectedTextColor = AppTheme.colors.TextColorLight,
        selectedTextColor = AppTheme.colors.TextColorLight,
    )

    ModalDrawerSheet(
        drawerContainerColor = AppTheme.colors.TopBarColor,
        drawerShape = RectangleShape,
        modifier = modifier.fillMaxWidth(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.pixelDp)
        ) {
            Spacer(modifier = Modifier.height(32.pixelDp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PixelIcon(
                    icon = R.drawable.ic_launcher,
                    modifier = Modifier
                        .size(58.pixelDp)
                )
            }

            Spacer(modifier = Modifier.height(8.pixelDp))

            Text(
                text = stringResource(R.string.app_name),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.TextColorLight
            )

            HomeDrawerItem(
                icon = R.drawable.ic_setting,
                label = stringResource(R.string.settings),
                selected = false,
                colors = drawerItemColors,
                onClick = onSettingsClick
            )

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(
                color = AppTheme.colors.Foreground2Color,
                modifier = Modifier.padding(vertical = 6.pixelDp)
            )

            HomeDrawerItem(
                icon = R.drawable.ic_info,
                label = stringResource(R.string.about),
                selected = false,
                colors = drawerItemColors,
                onClick = onAboutClick
            )

            HomeDrawerItem(
                icon = R.drawable.ic_left_arrow,
                label = stringResource(R.string.back),
                selected = false,
                colors = drawerItemColors,
                onClick = onBackClick
            )

            Spacer(modifier = Modifier.height(10.pixelDp))
        }
    }
}

@Composable
private fun HomeDrawerItem(
    @DrawableRes icon: Int,
    label: String,
    selected: Boolean,
    colors: androidx.compose.material3.NavigationDrawerItemColors,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelIcon(
                    icon = icon,
                    tint = AppTheme.colors.TextColorLight,
                )
                Spacer(modifier = Modifier.width(8.pixelDp))
                Text(text = label)
            }
        },
        shape = MaterialTheme.shapes.medium,
        selected = selected,
        onClick = onClick,
        colors = colors,
    )
}

