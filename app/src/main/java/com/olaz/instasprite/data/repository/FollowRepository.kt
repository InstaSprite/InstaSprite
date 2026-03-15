package com.olaz.instasprite.data.repository

import com.olaz.instasprite.di.RetrofitModule
import com.olaz.instasprite.data.network.api.FollowApi
import com.olaz.instasprite.data.network.getBodyOrError
import com.olaz.instasprite.data.network.model.FollowerDto
import com.olaz.instasprite.data.network.model.FollowingDto

import javax.inject.Inject

class FollowRepository @Inject constructor(
    private val followApi: FollowApi
) {

    suspend fun follow(username: String): Result<Boolean> {
        return try {
            val response = followApi.follow(username)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200 && body.data != null) {
                Result.success(body.data)
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Unknown error"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun unfollow(username: String): Result<Boolean> {
        return try {
            val response = followApi.unfollow(username)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200 && body.data != null) {
                Result.success(body.data)
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Unknown error"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getFollowings(username: String): Result<List<FollowingDto>> {
        return try {
            val response = followApi.getFollowings(username)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200 && body.data != null) {
                Result.success(body.data)
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Unknown error"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFollowers(username: String): Result<List<FollowerDto>> {
        return try {
            val response = followApi.getFollowers(username)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200 && body.data != null) {
                Result.success(body.data)
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Unknown error"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
