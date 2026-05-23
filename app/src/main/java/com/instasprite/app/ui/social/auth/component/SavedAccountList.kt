package com.instasprite.app.ui.social.auth.component

import com.instasprite.app.utils.pixelDp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.data.model.AccountPreferences
import com.instasprite.app.data.model.AccountType
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme


@Composable
fun SavedAccountList(
    onAccountSelected: (AccountPreferences) -> Unit,
    onAddAccountClick: () -> Unit,
    onRemoveAccountClick: (String) -> Unit,
    accounts: List<AccountPreferences>,
) {

    LocalContext.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.select_account),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.TextColorLight
        )
        Spacer(modifier = Modifier.height(16.pixelDp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            accounts.forEach { account ->
                SavedAccountItem(
                    account = account,
                    onClick = {
                        onAccountSelected(account)
                    },
                    onRemove = {
                        onRemoveAccountClick(account.username)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.pixelDp))

        Row(
            modifier = Modifier
                .clickable { onAddAccountClick() }
                .padding(6.pixelDp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PixelIcon(
                icon = R.drawable.ic_plus,
                contentDescription = null,
                tint = AppTheme.colors.InfoColor
            )
            Spacer(modifier = Modifier.width(6.pixelDp))
            Text(
                text = stringResource(R.string.log_into_another_account),
                color = AppTheme.colors.SelectedColor,
                fontWeight = FontWeight.Bold
            )
        }
    }

}

@Preview
@Composable
private fun Preview() {

    SavedAccountList(
        onAddAccountClick = { },
        onRemoveAccountClick = { },
        onAccountSelected = { },
        accounts = listOf(
            AccountPreferences(
                name = "John Doe",
                username = "alo alo",
                email = "alo@asd",
                avatarUrl = "dummy",
                accountType = AccountType.LOCAL
            ),
            AccountPreferences(
                name = "Dohn oe",
                username = "alo alo",
                email = "alo@asd",
                avatarUrl = "dummy",
                accountType = AccountType.LOCAL
            ),
        )
    )
}
