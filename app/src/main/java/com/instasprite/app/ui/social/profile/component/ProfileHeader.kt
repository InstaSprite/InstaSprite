package com.instasprite.app.ui.social.profile.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileHeader(
    username: String,
    isOwnProfile: Boolean,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LocalContext.current
    TopAppBar(
        title = {
            Text(
                text = username,
                color = AppTheme.colors.TextColorLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.back),
                    tint = AppTheme.colors.TextColorLight
                )
            }
        },
        actions = {
            // Hamburger menu button (always visible)
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.menu),
                    tint = AppTheme.colors.TextColorLight
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.colors.BackgroundColorDarker
        ),
        modifier = modifier
    )
}