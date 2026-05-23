package com.instasprite.app.ui.social.feed.component

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.instasprite.app.ui.components.shape.PixelShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.ui.components.composable.AsyncImageView
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun AvatarSelectionComponent(
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    LocalContext.current
    var showImagePicker by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImageSelected(uri)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.profile_picture),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.TextColorLight,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Avatar display
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(PixelShape(3))
                .border(
                    width = 3.dp,
                    color = AppTheme.colors.BottomBarColor,
                    shape = MaterialTheme.shapes.medium,
                )
                .background(AppTheme.colors.BackgroundColorDarker)
                .clickable { showImagePicker = true },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImageView(
                    imageUrl = selectedImageUri.toString(),
                    altText = "Selected Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(PixelShape(3)),
                )
            } else {
                PixelIcon(
                    icon = R.drawable.ic_plus,
                    contentDescription = stringResource(R.string.add_avatar),
                    scale = 2f,
                    tint = AppTheme.colors.Foreground2Color
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        // Gallery button
        OutlinedButton(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AppTheme.colors.TextColorLight
            ),
            border = BorderStroke(
                1.dp,
                AppTheme.colors.BottomBarColor
            )
        ) {
            PixelIcon(
                icon = R.drawable.ic_folder,
                contentDescription = stringResource(R.string.gallery),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.gallery))
        }


        // Remove image button (only show if image is selected)
        if (selectedImageUri != null) {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = { onImageSelected(null) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AppTheme.colors.Foreground2Color
                )
            ) {
                Text(stringResource(R.string.remove_image))
            }
        }
    }
}
