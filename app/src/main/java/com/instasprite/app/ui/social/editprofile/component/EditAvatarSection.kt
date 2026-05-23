package com.instasprite.app.ui.social.editprofile.component

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import com.instasprite.app.ui.components.shape.PixelShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.pixelDp

@Composable
fun EditAvatarSection(
    avatarUrl: String?,
    pendingAvatarUri: Uri?,
    onOpenSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val model: Any? = pendingAvatarUri ?: avatarUrl

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(100.pixelDp), contentAlignment = Alignment.Center) {
            AsyncImage(
                model = model,
                contentDescription = stringResource(R.string.profile_picture),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.pixelDp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(AppTheme.colors.Foreground2Color)
                    .border(1.pixelDp, AppTheme.colors.TextColorLight, PixelShape(3))
            )
            Box(
                modifier = Modifier
                    .size(22.pixelDp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(AppTheme.colors.TextColorLight)
                    .clickable(onClick = onOpenSheet)
                    .align(Alignment.BottomEnd),
//                    .offset(x = 2.pixelDp, y = 2.pixelDp),
                contentAlignment = Alignment.Center
            ) {
                PixelIcon(
                    icon = R.drawable.ic_edit,
                    contentDescription = stringResource(R.string.change_profile_photo),
                    tint = AppTheme.colors.TextColorDark,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditAvatarSectionPreview() {
    InstaSpriteTheme {
        Surface(color = AppTheme.colors.BackgroundColorDarker) {
            EditAvatarSection(
                avatarUrl = null,
                pendingAvatarUri = null,
                onOpenSheet = {}
            )
        }
    }
}
