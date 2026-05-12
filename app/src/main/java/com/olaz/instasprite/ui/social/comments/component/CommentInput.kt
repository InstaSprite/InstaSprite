package com.olaz.instasprite.ui.social.comments.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olaz.instasprite.R
import com.olaz.instasprite.ui.components.composable.AsyncImageView
import com.olaz.instasprite.ui.theme.AppTheme

@Composable
fun CommentInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    profileImageUrl: String? = null,
    replyingToUsername: String? = null,
    onCancelReply: () -> Unit = {}
) {
    LocalContext.current
    val canSend = text.trim().isNotEmpty()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.BackgroundColorDarker
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AnimatedVisibility(
                visible = replyingToUsername != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.Foreground0Color.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Replying to @$replyingToUsername",
                        color = AppTheme.colors.TextColorLight.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cancel),
                        tint = AppTheme.colors.TextColorLight.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .clickable { onCancelReply() }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val avatarModifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)

                if (!profileImageUrl.isNullOrBlank()) {
                    AsyncImageView(
                        imageUrl = profileImageUrl,
                        altText = stringResource(R.string.your_profile),
                        modifier = avatarModifier,
                    )
                } else {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher),
                        contentDescription = stringResource(R.string.your_profile),
                        modifier = avatarModifier,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppTheme.colors.BackgroundColor)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.add_a_comment),
                            color = AppTheme.colors.TextColorLight.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = AppTheme.colors.TextColorLight,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(AppTheme.colors.AccentButtonColor),
                        maxLines = 4
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (canSend) AppTheme.colors.AccentButtonColor else AppTheme.colors.BackgroundColor)
                        .clickable(enabled = canSend) { onSendClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.send),
                        tint = if (canSend) AppTheme.colors.BackgroundColorDarker else AppTheme.colors.TextColorLight.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    CommentInput(
        text = stringResource(R.string.this_is_a_comment),
        onTextChange = {},
        onSendClick = {},
    )
}
