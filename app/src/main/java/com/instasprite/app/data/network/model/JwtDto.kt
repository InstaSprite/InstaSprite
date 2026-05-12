package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName


data class JwtDto(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("accessToken")
    val accessToken: String,
    
    @SerializedName("refreshToken")
    val refreshToken: String,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("isFirstTime")
    val isFirstTime: Boolean? = null
)

