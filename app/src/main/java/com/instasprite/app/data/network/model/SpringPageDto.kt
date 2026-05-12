package com.instasprite.app.data.network.model

import com.google.gson.annotations.SerializedName

data class SpringPageDto<T> (
    @SerializedName("content")
    val content: List<T>,

    @SerializedName("last")
    val last: Boolean,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("totalElements")
    val totalElements: Long,
    
    @SerializedName("first")
    val first: Boolean,
    
    @SerializedName("numberOfElements")
    val numberOfElements: Int,
    
    @SerializedName("size")
    val size: Int,
    
    @SerializedName("number")
    val number: Int,
    
    @SerializedName("empty")
    val empty: Boolean
)
