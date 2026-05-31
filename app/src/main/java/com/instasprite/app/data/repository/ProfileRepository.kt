package com.instasprite.app.data.repository

import android.net.Uri
import com.instasprite.app.data.database.AppDatabase
import com.instasprite.app.data.model.FeedPostCrossRef
import com.instasprite.app.data.model.UserEntity
import com.instasprite.app.data.network.api.MemberPostApi
import com.instasprite.app.data.network.api.PostApi
import com.instasprite.app.data.network.api.ProfileApi
import com.instasprite.app.data.network.getBodyOrError
import com.instasprite.app.data.network.model.EditProfileRequestDto
import com.instasprite.app.data.network.model.EditProfileResponseDto
import com.instasprite.app.data.network.model.PostDto
import com.instasprite.app.data.network.model.UserProfileDto
import com.instasprite.app.data.network.model.toDomain
import com.instasprite.app.data.network.model.toDto
import com.instasprite.app.data.network.model.toEntity
import com.instasprite.app.data.network.model.toPostEntity
import com.instasprite.app.data.network.model.toUserEntity
import com.instasprite.app.data.network.safeApiCall
import com.instasprite.app.data.network.toResult
import com.instasprite.app.data.network.toResultMessage
import com.instasprite.app.di.RetrofitModule
import com.instasprite.app.domain.model.PostData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

open class ProfileRepository @Inject constructor(
    private val profileApi: ProfileApi,
    private val memberPostApi: MemberPostApi,
    private val postApi: PostApi,
    private val database: AppDatabase
) {
    private val userProfileDao = database.userProfileDao()
    private val postDao = database.postDao()
    private val userDao = database.userDao()

    suspend fun getCachedCurrentUserProfile(): UserProfileDto? {
        val entity = userProfileDao.getCurrentUser()
        val dto = entity?.toDto()
        // Ensure the current user also exists in the normalized users table
        if (dto != null) {
            userDao.insert(UserEntity(
                memberId = dto.memberId.toLong(),
                username = dto.memberUsername,
                name = dto.memberName,
                avatarUrl = dto.memberImage?.imageUrl ?: dto.memberImageUrl
            ))
        }
        return dto
    }

    suspend fun getCurrentUserProfile(): Result<UserProfileDto> = safeApiCall {
        val result = profileApi.getCurrentUserProfile().toResult()
        result.onSuccess { dto ->
            userProfileDao.insert(dto.toEntity())
            userDao.insert(UserEntity(
                memberId = dto.memberId.toLong(),
                username = dto.memberUsername,
                name = dto.memberName,
                avatarUrl = dto.memberImage?.imageUrl ?: dto.memberImageUrl
            ))
        }
        result
    }

    suspend fun getUserProfile(username: String): Result<UserProfileDto> = safeApiCall {
        val result = profileApi.getUserProfile(username).toResult()
        result.onSuccess { dto ->
            userDao.insert(UserEntity(
                memberId = dto.memberId.toLong(),
                username = dto.memberUsername,
                name = dto.memberName,
                avatarUrl = dto.memberImage?.imageUrl ?: dto.memberImageUrl
            ))
        }
        result
    }

    suspend fun getEditProfile(): Result<EditProfileResponseDto> = safeApiCall {
        profileApi.getEditProfile().toResult()
    }

    suspend fun editProfile(request: EditProfileRequestDto): Result<String> = safeApiCall {
        profileApi.editProfile(request).toResultMessage("Profile updated")
    }

    suspend fun uploadProfileImage(
        imageUri: Uri,
        context: android.content.Context
    ): Result<String> = safeApiCall {
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val file = File.createTempFile("profile_image", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("uploadedImage", file.name, requestFile)

        val response = profileApi.uploadProfileImage(body)
        file.delete()

        response.toResultMessage("Image uploaded")
    }

    suspend fun getRecentPostsByUsername(username: String): Result<List<PostData>> {
        val filter = "profile_$username"
        val networkResult = safeApiCall {
            val result = memberPostApi.getRecent15Posts(username).toResult()
            val domainResult = result.map { list ->
                list.filterNotNull().map { it.toDomain() }
            }
            result.onSuccess { posts ->
                postDao.clearByFilter(filter)
                val rawPosts = posts.filterNotNull()
                
                val cachedUser = userDao.getUserByUsername(username)
                val fallbackId = cachedUser?.memberId ?: 0L
                
                val users = rawPosts.mapNotNull { it.member?.toUserEntity() }
                val postEntities = rawPosts.map { it.toPostEntity(fallbackAuthorId = fallbackId) }
                val crossRefs = rawPosts.map { FeedPostCrossRef(it.postId, filter) }

                userDao.insertAll(users)
                postDao.insertAll(postEntities)
                postDao.insertFeedCrossRefs(crossRefs)
            }
            domainResult
        }
        
        return if (networkResult.isFailure) {
            val cached = postDao.getRecentPosts(filter).map { it.toDomain() }
            if (cached.isNotEmpty()) Result.success(cached) else networkResult
        } else {
            networkResult
        }
    }

    suspend fun getRecentSavedPosts(): Result<List<PostData>> {
        val filter = "profile_saved"
        val networkResult = safeApiCall {
            val result = memberPostApi.getRecent15SavedPosts().toResult()
            
            val hydratedResult = result.map { list ->
                val raw = list.filterNotNull()
                val hydrated = mutableListOf<PostDto>()
                for (p in raw) {
                    try {
                        val detailResp = postApi.getPost(p.postId)
                        val detailBody = detailResp.getBodyOrError(RetrofitModule.gson)
                        val detailed =
                            if (detailBody != null && detailBody.status == 200 && detailBody.data != null) {
                                detailBody.data
                            } else {
                                null
                            }
                        hydrated.add(detailed ?: p)
                    } catch (_: Exception) {
                        hydrated.add(p)
                    }
                }
                hydrated
            }
            hydratedResult.onSuccess { posts ->
                postDao.clearByFilter(filter)
                val users = posts.mapNotNull { it.member?.toUserEntity() }
                val postEntities = posts.map { it.toPostEntity() }
                val crossRefs = posts.map { FeedPostCrossRef(it.postId, filter) }

                userDao.insertAll(users)
                postDao.insertAll(postEntities)
                postDao.insertFeedCrossRefs(crossRefs)
            }
            hydratedResult.map { list -> list.map { it.toDomain() } }
        }
        
        return if (networkResult.isFailure) {
            val cached = postDao.getRecentPosts(filter).map { it.toDomain() }
            if (cached.isNotEmpty()) Result.success(cached) else networkResult
        } else {
            networkResult
        }
    }

    suspend fun getRecentTaggedPosts(username: String): Result<List<PostData>> = safeApiCall {
        memberPostApi.getRecent15TaggedPosts(username).toResult().map { list ->
            list.filterNotNull().map { it.toDomain() }
        }
    }
}
