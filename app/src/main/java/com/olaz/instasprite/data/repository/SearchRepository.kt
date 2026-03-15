package com.olaz.instasprite.data.repository

import com.olaz.instasprite.di.RetrofitModule
import com.olaz.instasprite.data.network.getBodyOrError
import com.olaz.instasprite.data.model.SearchData
import com.olaz.instasprite.data.model.SearchMemberData
import com.olaz.instasprite.data.model.SearchHashtagData
import com.olaz.instasprite.data.model.RecommendMemberData
import com.olaz.instasprite.data.network.api.SearchApi

import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val searchApi: SearchApi
) {

    suspend fun searchText(text: String): Result<List<SearchData>> {
        return try {
            val response = searchApi.searchText(text)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(emptyList())
            } else {
                val errorMessage = body?.message ?: "Search failed"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecommendMembers(): Result<List<RecommendMemberData>> {
        return try {
            val response = searchApi.getRecommendMembers()
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(emptyList())
            } else {
                val errorMessage = body?.message ?: "Failed to get recommend members"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMemberAutoComplete(text: String): Result<List<SearchMemberData>> {
        return try {
            val response = searchApi.getMemberAutoComplete(text)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(emptyList())
            } else {
                val errorMessage = body?.message ?: "Failed to get member autocomplete"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHashtagAutoComplete(text: String): Result<List<SearchHashtagData>> {
        return try {
            val response = searchApi.getHashtagAutoComplete(text)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(emptyList())
            } else {
                val errorMessage = body?.message ?: "Failed to get hashtag autocomplete"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markSearchedEntity(entityName: String, entityType: String): Result<String> {
        return try {
            val response = searchApi.markSearchedEntity(entityName, entityType)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(body.data ?: "Marked successfully")
            } else {
                val errorMessage = body?.message ?: "Failed to mark searched entity"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTop15RecentSearch(): Result<List<SearchData>> {
        return try {
            val response = searchApi.getTop15RecentSearch()
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(emptyList())
            } else {
                val errorMessage = body?.message ?: "Failed to get recent searches"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentSearch(page: Int): Result<List<SearchData>> {
        return try {
            val response = searchApi.getRecentSearch(page)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(emptyList())
            } else {
                val errorMessage = body?.message ?: "Failed to get recent searches"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRecentSearch(entityName: String, entityType: String): Result<String> {
        return try {
            val response = searchApi.deleteRecentSearch(entityName, entityType)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(body.data ?: "Deleted successfully")
            } else {
                val errorMessage = body?.message ?: "Failed to delete recent search"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllRecentSearch(): Result<String> {
        return try {
            val response = searchApi.deleteAllRecentSearch()
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200) {
                Result.success(body.data ?: "All recent searches deleted")
            } else {
                val errorMessage = body?.message ?: "Failed to delete all recent searches"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
