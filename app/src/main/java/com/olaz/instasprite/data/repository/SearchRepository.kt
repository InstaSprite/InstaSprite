package com.olaz.instasprite.data.repository

import com.olaz.instasprite.di.RetrofitModule
import com.olaz.instasprite.data.network.getBodyOrError
import com.olaz.instasprite.data.network.api.SearchApi
import com.olaz.instasprite.data.network.model.PostDto
import com.olaz.instasprite.data.network.model.SearchResponseDto

import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val searchApi: SearchApi
) {

    suspend fun search(query: String, page: Int = 1, size: Int = 10): Result<SearchResponseDto> {
        return try {
            val response = searchApi.search(query, page, size)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200 && body.data != null) {
                Result.success(body.data)
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Search failed"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun getTrendingPosts(): Result<List<PostDto>> {
        return try {
            val response = searchApi.getTrendingPosts()
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200 && body.data != null) {
                Result.success(body.data)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }
}
