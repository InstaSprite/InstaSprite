package com.olaz.instasprite.ui.social.auth.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
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
import com.olaz.instasprite.R
import com.olaz.instasprite.data.model.AccountPreferences
import com.olaz.instasprite.data.model.AccountType
import com.olaz.instasprite.ui.theme.CatppuccinUI


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
            color = CatppuccinUI.TextColorLight
        )
        Spacer(modifier = Modifier.height(24.dp))

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

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .clickable { onAddAccountClick() }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = CatppuccinUI.CurrentPalette.Flamingo
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log into another account",
                color = CatppuccinUI.SelectedColor,
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
