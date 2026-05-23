package com.instasprite.app.ui.social.editprofile.component

import com.instasprite.app.utils.pixelDp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

@Composable
fun EditProfileFields(
    displayName: String,
    bio: String,
    email: String,
    onDisplayNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = AppTheme.colors.TextColorLight,
        unfocusedTextColor = AppTheme.colors.TextColorLight,
        focusedBorderColor = AppTheme.colors.SelectedColor,
        unfocusedBorderColor = AppTheme.colors.Subtext0Color.copy(alpha = 0.4f),
        focusedLabelColor = AppTheme.colors.SelectedColor,
        unfocusedLabelColor = AppTheme.colors.Subtext0Color,
        cursorColor = AppTheme.colors.SelectedColor,
    )

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.display_name),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.Subtext0Color,
            modifier = Modifier.padding(bottom = 4.pixelDp)
        )
        OutlinedTextField(
            value = displayName,
            onValueChange = { if (it.length <= 50) onDisplayNameChange(it) },
            singleLine = true,
            colors = fieldColors,
            supportingText = {
                Text(
                    text = "${displayName.length}/50",
                    color = AppTheme.colors.Subtext0Color,
                    fontSize = 11.sp
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.pixelDp))

        Text(
            text = stringResource(R.string.bio),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.Subtext0Color,
            modifier = Modifier.padding(bottom = 4.pixelDp)
        )
        OutlinedTextField(
            value = bio,
            onValueChange = { if (it.length <= 150) onBioChange(it) },
            minLines = 3,
            maxLines = 5,
            colors = fieldColors,
            supportingText = {
                Text(
                    text = "${bio.length}/150",
                    color = AppTheme.colors.Subtext0Color,
                    fontSize = 11.sp
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.pixelDp))

        Text(
            text = stringResource(R.string.email),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.Subtext0Color,
            modifier = Modifier.padding(bottom = 4.pixelDp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    AppTheme.colors.BackgroundColor,
                    MaterialTheme.shapes.small
                )
                .padding(horizontal = 10.pixelDp, vertical = 10.pixelDp)
        ) {
            PixelIcon(
                icon = R.drawable.ic_lock,
                contentDescription = null,
                tint = AppTheme.colors.Subtext0Color,
                modifier = Modifier
                    .size(10.pixelDp)
                    .padding(end = 1.pixelDp)
            )
            Text(
                text = email,
                fontSize = 14.sp,
                color = AppTheme.colors.Subtext0Color,
                modifier = Modifier.padding(start = 6.pixelDp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditProfileFieldsPreview() {
    InstaSpriteTheme {
        EditProfileFields(
            displayName = "John Doe",
            bio = "Making pixel art one sprite at a time.",
            email = "john@example.com",
            onDisplayNameChange = {},
            onBioChange = {},
            modifier = Modifier.padding(10.pixelDp)
        )
    }
}
