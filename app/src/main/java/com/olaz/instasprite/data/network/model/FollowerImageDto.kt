package com.olaz.instasprite.data.network.model

import com.google.gson.annotations.SerializedName


data class FollowerImageDto(
    @SerializedName("imageUrl")
    val imageUrl: String,
    
    @SerializedName("imageType")
    val imageType: String,
    
    @SerializedName("imageName")
    val imageName: String,
    
    @SerializedName("imageUUID")
    val imageUUID: String,
    
    @SerializedName("imageWidth")
    val imageWidth: Int,
    
    @SerializedName("imageHeight")
    val imageHeight: Int
)

