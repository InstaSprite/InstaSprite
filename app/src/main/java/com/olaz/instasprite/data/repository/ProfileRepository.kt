package com.olaz.instasprite.data.repository

import android.net.Uri
import com.olaz.instasprite.data.network.api.MemberPostApi
import com.olaz.instasprite.data.network.api.PostApi
import com.olaz.instasprite.data.network.api.ProfileApi
import com.olaz.instasprite.data.network.getBodyOrError
import com.olaz.instasprite.data.network.model.EditProfileRequestDto
import com.olaz.instasprite.data.network.model.EditProfileResponseDto
import com.olaz.instasprite.data.network.model.UserProfileDto
import com.olaz.instasprite.data.network.model.toDomain
import com.olaz.instasprite.data.network.safeApiCall
import com.olaz.instasprite.data.network.toResult
import com.olaz.instasprite.data.network.toResultMessage
import com.olaz.instasprite.di.RetrofitModule
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

    suspend fun getCurrentUserProfile(): Result<UserProfileDto> = safeApiCall {
        profileApi.getCurrentUserProfile().toResult()
    }

    suspend fun getUserProfile(username: String): Result<UserProfileDto> = safeApiCall {
        profileApi.getUserProfile(username).toResult()
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

    suspend fun getRecentPostsByUsername(username: String): Result<List<PostData>> = safeApiCall {
        memberPostApi.getRecent15Posts(username).toResult().map { list ->
            list.filterNotNull().map { it.toDomain() }
        }
    }

    suspend fun getRecentSavedPosts(): Result<List<PostData>> = safeApiCall {
        memberPostApi.getRecent15SavedPosts().toResult().map { list ->
            val raw = list.filterNotNull().map { it.toDomain() }
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
                } catch (_: Exception) {
                    hydrated.add(p)
                }
            }
            hydrated
        }
    }

    suspend fun getRecentTaggedPosts(username: String): Result<List<PostData>> = safeApiCall {
        memberPostApi.getRecent15TaggedPosts(username).toResult().map { list ->
            list.filterNotNull().map { it.toDomain() }
        }
    }
}
