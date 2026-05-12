package com.instasprite.app.ui.social.auth.component

import androidx.compose.ui.res.stringResource

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.data.model.AccountPreferences
import com.instasprite.app.data.model.AccountType
import com.instasprite.app.ui.components.composable.AsyncImageView
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun SavedAccountItem(
    account: AccountPreferences,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.BackgroundColor)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AsyncImageView(
                imageUrl = account.avatarUrl ?: "",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.Foreground0Color)
            )

            if (account.accountType == AccountType.GOOGLE) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(Alignment.BottomEnd)
                        .size(18.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = stringResource(R.string.remove_account),
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(16.dp)
                    )
                }

            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = account.name ?: "Unknown",
                    color = AppTheme.colors.TextColorLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = '@' + account.username,
                    color = AppTheme.colors.Subtext0Color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = account.email!!,
                color = AppTheme.colors.TextColorLight.copy(alpha = 0.7f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.remove_account),
                tint = AppTheme.colors.DismissButtonColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    SavedAccountItem(
        account = AccountPreferences(
            name = "John Doe",
            email = "Test@sd",
            username = "sda",
            avatarUrl = "dummy",
            accountType = AccountType.GOOGLE
        ),
        {},
        {}
    )
}