package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName


data class MemberImageDto(
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    
    @SerializedName("imageType")
    val imageType: String? = null,
    
    @SerializedName("imageName")
    val imageName: String? = null,
    
    @SerializedName("imageUUID")
    val imageUUID: String? = null,
    
    @SerializedName("imageWidth")
    val imageWidth: Int? = null,
    
    @SerializedName("imageHeight")
    val imageHeight: Int? = null
)

