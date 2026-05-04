package com.olaz.instasprite.data.repository

import com.olaz.instasprite.data.network.api.SearchApi
import com.olaz.instasprite.data.network.model.PostDto
import com.olaz.instasprite.data.network.model.SearchResponseDto
import com.olaz.instasprite.data.network.safeApiCall
import com.olaz.instasprite.data.network.toResult
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
