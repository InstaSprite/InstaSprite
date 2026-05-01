package com.olaz.instasprite.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import coil3.decode.DecodeUtils.calculateInSampleSize
import coil3.size.Scale
import com.olaz.instasprite.di.RetrofitModule
import com.olaz.instasprite.data.network.api.PostApi
import com.olaz.instasprite.data.network.getBodyOrError
import com.olaz.instasprite.data.network.model.PostDto
import com.olaz.instasprite.data.network.model.PageDto
import com.olaz.instasprite.data.network.model.toDomain
import com.olaz.instasprite.domain.model.PageData
import com.olaz.instasprite.domain.model.PostData
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

class PostRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val postApi : PostApi
) {

    suspend fun getPost(postId: Long): Result<PostData> {
        return try {
            val response = postApi.getPost(postId)
            val body = response.getBodyOrError(RetrofitModule.gson)

            if (body != null && body.status == 200 && body.data != null) {
                val post = body.data.toDomain()
                Result.success(post)
            } else {
                val errorMessage = body?.message ?: "Failed to get post detail"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun uploadPostWithUris(
        content: String,
        images: List<Uri>,
        altTexts: List<String>,
        commentFlag: Boolean,
    ): Result<String> {
        return try {
            val imageParts = images.mapIndexed { index, uri ->
                val fileName = "post_image_$index"
                val file = compressImageIfNeeded(
                    context = context,
                    uri = uri,
                    outputFileName = fileName
                )

                val requestFile = file
                    .asRequestBody("image/jpeg".toMediaType())

                MultipartBody.Part.createFormData(
                    name = "postImages",
                    filename = file.name,
                    body = requestFile
                )
            }

            val contentBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
            val altTextParts = altTexts.mapIndexed { index, altText ->
                MultipartBody.Part.createFormData("altTexts", altText)
            }
            val commentFlagBody = commentFlag.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val response = postApi.uploadPost(
                content = contentBody,
                postImages = imageParts,
                altTexts = altTextParts,
                commentFlag = commentFlagBody
            )
            val body = response.getBodyOrError<Any>(RetrofitModule.gson)

            if (body != null && body.status == 200) {
                val postId = if (body.data is Map<*, *>) {
                    val dataMap = body.data as Map<*, *>
                    val postIdValue = dataMap["postId"]
                    if (postIdValue is Number) {
                        postIdValue.toLong()
                    } else {
                        null
                    }
                } else {
                    null
                }
                Result.success("Post uploaded successfully with ID: $postId")
            } else {
                val errorMessage = body?.message ?: "Failed to upload post"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    private fun uriToFile(uri: Uri, baseFileName: String): File {
        val file = File(context.cacheDir, "$baseFileName.${getFileExtension(uri)}")
        file.createNewFile()

        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file
    }

    fun compressImageIfNeeded(
        context: Context,
        uri: Uri,
        outputFileName: String,
        maxSizeBytes: Long = 1_500_000, // 1.5 MB
        maxDimension: Int = 1280,
        quality: Int = 80
    ): File {
        // Check original size
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val originalBytes = inputStream.available()
        inputStream.close()

        // If already small, just copy with desired name
        if (originalBytes <= maxSizeBytes) {
            return uriToFile(uri, outputFileName)
        }

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        options.inSampleSize = calculateInSampleSize(
            options.outWidth,
            options.outHeight,
            maxDimension,
            maxDimension,
            Scale.FIT
        )
        options.inJustDecodeBounds = false

        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IllegalStateException("Failed to decode bitmap")

        val scaledBitmap = scaleBitmap(bitmap, maxDimension)

        val outputFile = File(
            context.cacheDir,
            "$outputFileName.jpg"
        )

        FileOutputStream(outputFile).use {
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, it)
        }

        bitmap.recycle()
        if (bitmap != scaledBitmap) scaledBitmap.recycle()

        return outputFile
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun getFileExtension(uri: Uri): String {
        val path = uri.path ?: return "png"
        return path.substringAfterLast('.', "png").lowercase()
    }

    private fun getMediaTypeFromUri(uri: Uri): String {
        val extension = getFileExtension(uri)
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            else -> "image/png"
        }
    }

    suspend fun getPostPage(lastPostId: Long?): Result<PageData> {
        return try {
            val response = postApi.getPostPage(lastPostId)
            val body = response.getBodyOrError<PageDto>(RetrofitModule.gson)

            if (body != null && body.status == 200 && body.data != null) {
                val posts = body.data.toDomain()
                Result.success(posts)
            } else {
                val errorMessage = body?.message ?: "Failed to get posts"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun getRecent10Posts(): Result<List<PostData>> {
        return try {
            val response = postApi.getRecent10Posts()
            val body = response.getBodyOrError<List<com.olaz.instasprite.data.network.model.PostDto>>(RetrofitModule.gson)

            if (body != null && body.status == 200 && body.data != null) {
                val posts = body.data.map { it.toDomain() }
                Result.success(posts)
            } else {
                val errorMessage = body?.message ?: "Failed to get recent posts"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun getRecentPostsPage(lastPostId: Long?): Result<PageData> {
        return try {
            val response = postApi.getRecentPostsPage(lastPostId)
            val body = response.getBodyOrError<PageDto>(RetrofitModule.gson)

            if (body != null && body.status == 200 && body.data != null) {
                val posts = body.data.toDomain()
                Result.success(posts)
            } else {
                val errorMessage = body?.message ?: "Failed to get recent posts page"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun likePost(postId: Long): Result<String> {
        return try {
            val response = postApi.likePost(postId)
            val body = response.getBodyOrError<String>(RetrofitModule.gson)

            if (body != null && body.status == 200) {
                Result.success(body.data ?: "Post liked successfully")
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Unknown error"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun unlikePost(postId: Long): Result<String> {
        return try {
            val response = postApi.unlikePost(postId)
            val body = response.getBodyOrError<String>(RetrofitModule.gson)

            if (body != null && body.status == 200) {
                Result.success(body.data ?: "Post unliked successfully")
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Unknown error"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun bookmarkPost(postId: Long): Result<String> {
        return try {
            val response = postApi.bookmarkPost(postId)
            val body = response.getBodyOrError<String>(RetrofitModule.gson)

            if (body != null && body.status == 200) {
                Result.success(body.data ?: "Post bookmarked successfully")
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Unknown error"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun unBookmarkPost(postId: Long): Result<String> {
        return try {
            val response = postApi.unBookmarkPost(postId)
            val body = response.getBodyOrError<String>(RetrofitModule.gson)

            if (body != null && body.status == 200) {
                Result.success(body.data ?: "Post unbookmarked successfully")
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Unknown error"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: Long): Result<Boolean> {
        return try {
            val response = postApi.deletePost(postId)
            val body = response.getBodyOrError<String>(RetrofitModule.gson)

            if (body != null && body.status == 200) {
                Result.success(true)
            } else {
                val errorCode = body?.code ?: response.code().toString()
                val errorMessage = body?.message ?: "Unknown error"
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun getMostLikedPost(): Result<PostData> {
        return try {
            val response = postApi.getMostLikedPost()
            val body = response.getBodyOrError<PostDto>(RetrofitModule.gson)

            if (body != null && body.status == 200 && body.data != null) {
                val post = body.data.toDomain()
                Result.success(post)
            } else {
                val errorMessage = body?.message ?: "Failed to get most liked post"
                val errorCode = body?.code ?: response.code().toString()
                Result.failure(Exception("$errorCode: $errorMessage"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }
}

