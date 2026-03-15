package com.olaz.instasprite.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AccountPreferences(
    val username: String,
    val name: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val accountType: AccountType,
    val isVerified: Boolean = false
)

@Serializable
enum class AccountType {
    LOCAL,
    GOOGLE
}

@Serializable
data class AccountMapPreferences(
    val accounts: Map<String, AccountPreferences> = emptyMap(), // Key = username
    val activeAccountUsername: String? = null
)


