package com.olaz.instasprite.ui.social.comments.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.olaz.instasprite.R
import com.olaz.instasprite.ui.components.composable.AsyncImageView
import com.olaz.instasprite.ui.components.composable.CustomTextField
import com.olaz.instasprite.ui.theme.CatppuccinUI

@Composable
fun CommentInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    profileImageUrl: String? = null
) {
    LocalContext.current
    val canSend = text.trim().isNotEmpty()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        colors = CardDefaults.cardColors(
            containerColor = CatppuccinUI.BackgroundColor
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImageView(
                imageUrl = profileImageUrl ?: "",
                altText = stringResource(R.string.your_profile),
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
            )

            Spacer(modifier = Modifier.width(12.dp))

            CustomTextField(
                value = text,
                onValueChange = onTextChange,
                label = stringResource(R.string.comment),
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Default,
                maxLines = 3
            )

            IconButton(onClick = { if (canSend) onSendClick() }) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.send),
                    tint = CatppuccinUI.AccentButtonColor
                )
            }
        }
    }
}


@Preview
@Composable
private fun Preview() {
    CommentInput(
        text = "This is a comment",
        onTextChange = {},
        onSendClick = {},

        )
}

