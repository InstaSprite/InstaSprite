package com.olaz.instasprite.ui.social.profile.component

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
import com.olaz.instasprite.R
import com.olaz.instasprite.ui.theme.CatppuccinUI

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
                color = CatppuccinUI.TextColorLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.back),
                    tint = CatppuccinUI.TextColorLight
                )
            }
        },
        actions = {
            // Hamburger menu button (always visible)
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.menu),
                    tint = CatppuccinUI.TextColorLight
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CatppuccinUI.BackgroundColorDarker
        ),
        modifier = modifier
    )
}