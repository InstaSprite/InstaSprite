package com.instasprite.app.ui.home.component

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.ui.social.feed.ProfileImageState
import com.instasprite.app.ui.social.feed.ProfileState
import com.instasprite.app.ui.social.feed.component.ProfileImage
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun HomeDrawer(
    isLoggedIn: Boolean,
    profileState: ProfileState,
    profileImageState: ProfileImageState,
    username: String?,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLoginClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val drawerItemColors = NavigationDrawerItemDefaults.colors(
        unselectedContainerColor = Color.Transparent,
        selectedContainerColor = AppTheme.colors.Foreground0Color.copy(alpha = 0.85f),
        unselectedIconColor = AppTheme.colors.TextColorLight,
        selectedIconColor = AppTheme.colors.TextColorLight,
        unselectedTextColor = AppTheme.colors.TextColorLight,
        selectedTextColor = AppTheme.colors.TextColorLight,
    )

    val memberName = profileState.memberName.ifBlank {
        username?.takeIf { it.isNotBlank() } ?: stringResource(R.string.app_name)
    }
    val memberUsername = profileState.memberUsername.ifBlank { username.orEmpty() }

    ModalDrawerSheet(
        drawerContainerColor = AppTheme.colors.TopBarColor,
        drawerShape = RectangleShape,
        modifier = modifier.fillMaxWidth(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher),
                contentDescription = stringResource(R.string.app_name),
                tint = Color.Unspecified,
                modifier = Modifier
                    .padding(16.dp)
                    .size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoggedIn) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ProfileImage(
                        state = profileImageState,
                        size = 96.dp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (profileState.isLoading && memberUsername.isBlank()) {
                    Text(
                        text = stringResource(R.string.loading),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.TextColorLight.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = memberName,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppTheme.colors.TextColorLight
                    )

                    if (memberUsername.isNotBlank()) {
                        Text(
                            text = "@$memberUsername",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.TextColorLight.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            HomeDrawerItem(
                icon = Icons.Default.Person,
                label = if (isLoggedIn) stringResource(R.string.profile) else stringResource(R.string.login),
                selected = false,
                colors = drawerItemColors,
                onClick = if (isLoggedIn) onProfileClick else onLoginClick
            )

            HomeDrawerItem(
                icon = Icons.Default.Home,
                label = stringResource(R.string.home),
                selected = true,
                colors = drawerItemColors,
                onClick = onHomeClick
            )

            HomeDrawerItem(
                icon = Icons.Default.Notifications,
                label = stringResource(R.string.notifications),
                selected = false,
                colors = drawerItemColors,
                onClick = onNotificationsClick
            )

            HomeDrawerItem(
                icon = Icons.Default.Search,
                label = "Search",
                selected = false,
                colors = drawerItemColors,
                onClick = onSearchClick
            )

            HomeDrawerItem(
                icon = Icons.Default.Settings,
                label = stringResource(R.string.settings),
                selected = false,
                colors = drawerItemColors,
                onClick = onSettingsClick
            )

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(
                color = AppTheme.colors.TextColorLight.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            HomeDrawerItem(
                icon = Icons.Default.Info,
                label = stringResource(R.string.about),
                selected = false,
                colors = drawerItemColors,
                onClick = onAboutClick
            )

            if (isLoggedIn) {
                HomeDrawerItem(
                    icon = Icons.Default.Delete,
                    label = stringResource(R.string.logout),
                    selected = false,
                    colors = drawerItemColors,
                    onClick = onLogoutClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HomeDrawerItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    colors: androidx.compose.material3.NavigationDrawerItemColors,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppTheme.colors.TextColorLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = label)
            }
        },
        selected = selected,
        onClick = onClick,
        colors = colors,
    )
}

