package com.olaz.instasprite.data.network.model

import com.google.gson.annotations.SerializedName


data class PostImageDto(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("postImageUrl")
    val postImageUrl: String,  // Relative URL from API
    
    @SerializedName("altText")
    val altText: String? = null,
    
    @SerializedName("imageWidth")
    val imageWidth: Int? = null,
    
    @SerializedName("imageHeight")
    val imageHeight: Int? = null,
    
    @SerializedName("postTags")
    val postTags: List<PostTagDto>? = null,

    @SerializedName("dominantColor")
    val dominantColor: String? = null
)

