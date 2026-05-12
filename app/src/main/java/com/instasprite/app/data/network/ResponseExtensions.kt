package com.instasprite.app.data.network

import com.google.gson.Gson
import com.instasprite.app.data.network.model.ResultResponse
import retrofit2.Response

fun <T> Response<ResultResponse<T>>.getBodyOrError(gson: Gson): ResultResponse<T>? {
    return if (isSuccessful) {
        body()
    } else {
        val errorBodyString = errorBody()?.string()
        if (errorBodyString != null) {
            try {
                @Suppress("UNCHECKED_CAST")
                gson.fromJson(errorBodyString, ResultResponse::class.java) as? ResultResponse<T>
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
}

