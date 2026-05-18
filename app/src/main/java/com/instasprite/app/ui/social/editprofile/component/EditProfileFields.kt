package com.instasprite.app.ui.social.editprofile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
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
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = displayName,
            onValueChange = { if (it.length <= 50) onDisplayNameChange(it) },
            singleLine = true,
            colors = fieldColors,
            shape = RoundedCornerShape(12.dp),
            supportingText = {
                Text(
                    text = "${displayName.length}/50",
                    color = AppTheme.colors.Subtext0Color,
                    fontSize = 11.sp
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.bio),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.Subtext0Color,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = bio,
            onValueChange = { if (it.length <= 150) onBioChange(it) },
            minLines = 3,
            maxLines = 5,
            colors = fieldColors,
            shape = RoundedCornerShape(12.dp),
            supportingText = {
                Text(
                    text = "${bio.length}/150",
                    color = AppTheme.colors.Subtext0Color,
                    fontSize = 11.sp
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.email),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.Subtext0Color,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    AppTheme.colors.BackgroundColor,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = AppTheme.colors.Subtext0Color,
                modifier = Modifier
                    .size(16.dp)
                    .padding(end = 0.dp)
            )
            Text(
                text = email,
                fontSize = 14.sp,
                color = AppTheme.colors.Subtext0Color,
                modifier = Modifier.padding(start = 10.dp)
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
            modifier = Modifier.padding(16.dp)
        )
    }
}
