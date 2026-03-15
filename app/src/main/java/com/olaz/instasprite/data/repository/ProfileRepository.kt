package com.olaz.instasprite.data.repository

import android.net.Uri
import android.util.Log
import com.olaz.instasprite.di.RetrofitModule
import com.olaz.instasprite.data.network.api.MemberPostApi
import com.olaz.instasprite.data.network.api.PostApi
import com.olaz.instasprite.data.network.api.ProfileApi
import com.olaz.instasprite.data.network.getBodyOrError
import com.olaz.instasprite.data.network.model.EditProfileRequestDto
import com.olaz.instasprite.data.network.model.EditProfileResponseDto
import com.olaz.instasprite.data.network.model.ResultResponse
import com.olaz.instasprite.data.network.model.UserProfileDto
import com.olaz.instasprite.data.network.model.toDomain
import com.olaz.instasprite.domain.model.PostData
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
    private val postApi: PostApi
) {

    suspend fun getCurrentUserProfile(): ResultResponse<UserProfileDto> {
        val response = profileApi.getCurrentUserProfile()
        return response.getBodyOrError(RetrofitModule.gson) ?: ResultResponse(
            500,
            "UNKNOWN",
            "No response body",
            null
        )
    }

    suspend fun getUserProfile(username: String): ResultResponse<UserProfileDto> {
        val response = profileApi.getUserProfile(username)
        return response.getBodyOrError(RetrofitModule.gson) ?: ResultResponse(
            500,
            "UNKNOWN",
            "No response body",
            null
        )
    }

    suspend fun getEditProfile(): ResultResponse<EditProfileResponseDto> {
        val response = profileApi.getEditProfile()
        return response.getBodyOrError(RetrofitModule.gson) ?: ResultResponse(
            500,
            "UNKNOWN",
            "No response body",
            null
        )
    }

    suspend fun editProfile(request: EditProfileRequestDto): ResultResponse<String> {
        val response = profileApi.editProfile(request)
        return response.getBodyOrError(RetrofitModule.gson) ?: ResultResponse(
            500,
            "UNKNOWN",
            "No response body",
            null
        )
    }

    suspend fun uploadProfileImage(
        imageUri: Uri,
        context: android.content.Context
    ): ResultResponse<String> {
        return try {
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

            response.getBodyOrError(RetrofitModule.gson) ?: ResultResponse(
                500,
                "UPLOAD_ERROR",
                "No response body",
                null
            )
        } catch (e: Exception) {
            ResultResponse(
                status = 500,
                code = "UPLOAD_ERROR",
                message = e.message ?: "Failed to upload image",
                data = null
            )
        }
    }

    suspend fun getRecentPostsByUsername(username: String): Result<List<PostData>> {
        return try {
            val response = memberPostApi.getRecent15Posts(username)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200 && body.data != null) {
                val posts = body.data.filterNotNull().map { it.toDomain() }
                Result.success(posts)
            } else {
                val errorMessage = body?.message ?: "Failed to load posts"
                android.util.Log.e(
                    "ProfileRepository",
                    "Failed to get recent posts: status=${body?.status}, message=$errorMessage"
                )
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileRepository", "Error getting recent posts", e)
            Result.failure(e)
        }
    }

    suspend fun getRecentSavedPosts(): Result<List<PostData>> {
        return try {
            val response = memberPostApi.getRecent15SavedPosts()
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200 && body.data != null) {
                val raw = body.data.filterNotNull().map { it.toDomain() }
                val hydrated = mutableListOf<PostData>()
                for (p in raw) {
                    try {
                        val detailResp = postApi.getPost(p.postId)
                        val detailBody = detailResp.getBodyOrError(RetrofitModule.gson)
                        val detailed =
                            if (detailBody != null && detailBody.status == 200 && detailBody.data != null) {
                                detailBody.data.toDomain()
                            } else {
                                null
                            }
                        hydrated.add(detailed ?: p)
                    } catch (e: Exception) {
                        Log.w("ProfileRepository", "Failed to hydrate post ${p.postId}", e)
                        hydrated.add(p)
                    }
                }
                Result.success(hydrated)
            } else {
                val errorMessage = body?.message ?: "Failed to load saved posts"
                Log.e(
                    "ProfileRepository",
                    "Failed to get saved posts: status=${body?.status}, message=$errorMessage"
                )
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error getting saved posts", e)
            Result.failure(e)
        }
    }

    suspend fun getRecentTaggedPosts(username: String): Result<List<PostData>> {
        return try {
            val response = memberPostApi.getRecent15TaggedPosts(username)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body != null && body.status == 200 && body.data != null) {
                val posts = body.data.filterNotNull().map { it.toDomain() }
                Result.success(posts)
            } else {
                val errorMessage = body?.message ?: "Failed to load tagged posts"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

