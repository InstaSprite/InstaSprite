package com.instasprite.app.data.repository

import androidx.datastore.core.DataStore
import com.instasprite.app.data.model.AccountMapPreferences
import com.instasprite.app.data.model.AccountPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val dataStore: DataStore<AccountMapPreferences>
) {

    fun getAccounts(): Flow<List<AccountPreferences>> {
        return dataStore.data.map { it.accounts.values.toList() }
    }

    fun getAccount(username: String): Flow<AccountPreferences?> {
        return dataStore.data.map { it.accounts[username] }
    }


    suspend fun addAccount(account: AccountPreferences) {
        dataStore.updateData { prefs ->
            val newMap = prefs.accounts + (account.username to account)

            prefs.copy(
                accounts = newMap,
                activeAccountUsername = account.username
            )
        }
    }

    suspend fun updateAccount(
        username: String,
        updateBlock: (AccountPreferences) -> AccountPreferences
    ) {
        dataStore.updateData { prefs ->
            val existingAccount = prefs.accounts[username]

            if (existingAccount != null) {
                val updatedAccount = updateBlock(existingAccount)

                val newMap = prefs.accounts + (username to updatedAccount)
                prefs.copy(accounts = newMap)
            } else {
                prefs
            }
        }
    }

    suspend fun removeAccount(username: String) {
        dataStore.updateData { prefs ->
            val newMap = prefs.accounts - username

            val newActive = if (prefs.activeAccountUsername == username) {
                newMap.keys.firstOrNull()
            } else {
                prefs.activeAccountUsername
            }

            prefs.copy(accounts = newMap, activeAccountUsername = newActive)
        }
    }
}