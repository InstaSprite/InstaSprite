package com.olaz.instasprite.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TokenPreferences(
    val keyAccessToken: String? = null,
    val keyRefreshToken: String? = null,
    val keyTokenType: String? = null,
    val keyUsername: String? = null
)