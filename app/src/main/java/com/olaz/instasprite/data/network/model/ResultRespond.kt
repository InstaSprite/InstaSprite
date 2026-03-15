package com.olaz.instasprite.data.network.model

/**
 * Wrapper for API responses
 */
data class ResultResponse<T>(
    val status: Int,
    val code: String,
    val message: String,
    val data: T?
)