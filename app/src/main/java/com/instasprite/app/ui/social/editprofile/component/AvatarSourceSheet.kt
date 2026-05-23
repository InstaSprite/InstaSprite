package com.instasprite.app.ui.social.editprofile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarSourceSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onPickFromDevice: () -> Unit,
    onPickFromSprite: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.BackgroundColor,
        tonalElevation = 0.dp,
    ) {
        AvatarSourceSheetContent(
            onDismiss = onDismiss,
            onPickFromDevice = onPickFromDevice,
            onPickFromSprite = onPickFromSprite,
        )
    }
}

@Composable
private fun AvatarSourceSheetContent(
    onDismiss: () -> Unit,
    onPickFromDevice: () -> Unit,
    onPickFromSprite: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.change_profile_photo),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.TextColorLight,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
        )
        HorizontalDivider(color = AppTheme.colors.Subtext0Color.copy(alpha = 0.15f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onDismiss()
                    onPickFromSprite()
                }
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            PixelIcon(
                icon = R.drawable.ic_canvas,
                contentDescription = null,
                tint = AppTheme.colors.SelectedColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.pick_from_sprite),
                fontSize = 15.sp,
                color = AppTheme.colors.TextColorLight
            )
        }
        HorizontalDivider(color = AppTheme.colors.Subtext0Color.copy(alpha = 0.08f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onDismiss()
                    onPickFromDevice()
                }
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            PixelIcon(
                icon = R.drawable.ic_folder,
                contentDescription = null,
                tint = AppTheme.colors.LinkColor,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.pick_from_device),
                fontSize = 15.sp,
                color = AppTheme.colors.TextColorLight
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarSourceSheetPreview() {
    InstaSpriteTheme {
        Box(modifier = Modifier.background(AppTheme.colors.BackgroundColor)) {
            AvatarSourceSheetContent(
                onDismiss = {},
                onPickFromDevice = {},
                onPickFromSprite = {},
            )
        }
    }
}
