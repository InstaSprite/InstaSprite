package com.olaz.instasprite.data.network.model

import com.google.gson.annotations.SerializedName

data class OtpEnrollmentDto(
    @SerializedName("enabled")
    val enabled: Boolean,
    
    @SerializedName("issuer")
    val issuer: String,
    
    @SerializedName("accountName")
    val accountName: String,
    
    @SerializedName("secret")
    val secret: String,
    
    @SerializedName("otpauthUri")
    val otpauthUri: String,
    
    @SerializedName("qrCodePngBase64")
    val qrCodePngBase64: String,
    
    @SerializedName("encryptionEnabled")
    val encryptionEnabled: Boolean
)

