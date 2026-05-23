package com.instasprite.app.ui.social.comments.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.RetronFont
import com.instasprite.app.utils.noRippleClickable

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
                        color = AppTheme.colors.Foreground2Color,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    PixelIcon(
                        icon = R.drawable.ic_close,
                        contentDescription = stringResource(R.string.cancel),
                        tint = AppTheme.colors.Foreground2Color,
                        modifier = Modifier
                            .noRippleClickable { onCancelReply() }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                            color = AppTheme.colors.TextColorLight,
                            fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = AppTheme.colors.TextColorLight,
                            fontFamily = RetronFont,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(AppTheme.colors.AccentButtonColor),
                        maxLines = 4
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                PixelIcon(
                    icon = R.drawable.ic_right_arrow,
                    contentDescription = stringResource(R.string.send),
                    tint = if (canSend) AppTheme.colors.AccentButtonColor else AppTheme.colors.Foreground2Color,
                    modifier = Modifier.noRippleClickable { if (canSend) onSendClick() }
                )
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
