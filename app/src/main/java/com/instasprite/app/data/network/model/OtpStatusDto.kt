package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName

data class OtpStatusDto(
    @SerializedName("enabled")
    val enabled: Boolean
)

