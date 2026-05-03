package com.olaz.instasprite.data.repository

import com.olaz.instasprite.data.network.api.FollowApi
import com.olaz.instasprite.data.network.model.FollowerDto
import com.olaz.instasprite.data.network.model.SpringPageDto
import com.olaz.instasprite.data.network.safeApiCall
import com.olaz.instasprite.data.network.toResult
import javax.inject.Inject

class FollowRepository @Inject constructor(
    private val followApi: FollowApi
) {

    suspend fun follow(username: String): Result<Boolean> = safeApiCall {
        followApi.follow(username).toResult()
    }

    suspend fun unfollow(username: String): Result<Boolean> = safeApiCall {
        followApi.unfollow(username).toResult()
    }

    suspend fun getFollowings(username: String): Result<SpringPageDto<FollowerDto>> = safeApiCall {
        followApi.getFollowings(username).toResult()
    }

    suspend fun getFollowers(username: String): Result<SpringPageDto<FollowerDto>> = safeApiCall {
        followApi.getFollowers(username).toResult()
    }
}
