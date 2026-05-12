package com.instasprite.app.data.repository

import com.instasprite.app.data.network.api.SearchApi
import com.instasprite.app.data.network.model.PostDto
import com.instasprite.app.data.network.model.SearchResponseDto
import com.instasprite.app.data.network.safeApiCall
import com.instasprite.app.data.network.toResult
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val searchApi: SearchApi
) {

    suspend fun search(query: String, page: Int = 1, size: Int = 10): Result<SearchResponseDto> = safeApiCall {
        searchApi.search(query, page, size).toResult()
    }

    suspend fun getTrendingPosts(): Result<List<PostDto>> = safeApiCall {
        searchApi.getTrendingPosts().toResult()
    }
}
