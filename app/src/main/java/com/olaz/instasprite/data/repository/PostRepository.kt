package com.olaz.instasprite.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import coil3.decode.DecodeUtils.calculateInSampleSize
import coil3.size.Scale
import com.olaz.instasprite.data.network.api.PostApi
import com.olaz.instasprite.data.network.model.PostDto
import com.olaz.instasprite.data.network.model.PageDto
import com.olaz.instasprite.data.network.model.SpringPageDto
import com.olaz.instasprite.data.network.model.toDomain
import com.olaz.instasprite.data.network.safeApiCall
import com.olaz.instasprite.data.network.toResult
import com.olaz.instasprite.data.network.toResultMessage
import com.olaz.instasprite.data.network.toResultUnit
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
    private val postApi: PostApi
) {

    suspend fun getPost(postId: Long): Result<PostData> = safeApiCall {
        postApi.getPost(postId).toResult().map { it.toDomain() }
    }

    suspend fun uploadPostWithUris(
        content: String,
        images: List<Uri>,
        altTexts: List<String>,
        commentFlag: Boolean,
    ): Result<String> = safeApiCall {
        val imageParts = images.mapIndexed { index, uri ->
            val file = compressImageIfNeeded(
                context = context,
                uri = uri,
                outputFileName = "post_image_$index"
            )
            val requestFile = file.asRequestBody("image/jpeg".toMediaType())
            MultipartBody.Part.createFormData(
                name = "postImages",
                filename = file.name,
                body = requestFile
            )
        }

        val contentBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val altTextParts = altTexts.map { altText ->
            MultipartBody.Part.createFormData("altTexts", altText)
        }
        val commentFlagBody = commentFlag.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val response = postApi.uploadPost(
            content = contentBody,
            postImages = imageParts,
            altTexts = altTextParts,
            commentFlag = commentFlagBody
        )
        response.toResultMessage("Post uploaded successfully")
    }

    suspend fun getPostPage(lastPostId: Long?): Result<PageData> = safeApiCall {
        postApi.getPostPage(lastPostId).toResult().map { it.toDomain() }
    }

    suspend fun getRecent10Posts(): Result<List<PostData>> = safeApiCall {
        postApi.getRecent10Posts().toResult().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getRecentPostsPage(lastPostId: Long?): Result<PageData> = safeApiCall {
        postApi.getRecentPostsPage(lastPostId).toResult().map { it.toDomain() }
    }

    suspend fun likePost(postId: Long): Result<String> = safeApiCall {
        postApi.likePost(postId).toResultMessage("Post liked")
    }

    suspend fun unlikePost(postId: Long): Result<String> = safeApiCall {
        postApi.unlikePost(postId).toResultMessage("Post unliked")
    }

    suspend fun bookmarkPost(postId: Long): Result<String> = safeApiCall {
        postApi.bookmarkPost(postId).toResultMessage("Post bookmarked")
    }

    suspend fun unBookmarkPost(postId: Long): Result<String> = safeApiCall {
        postApi.unBookmarkPost(postId).toResultMessage("Post unbookmarked")
    }

    suspend fun deletePost(postId: Long): Result<Boolean> = safeApiCall {
        postApi.deletePost(postId).toResultUnit().map { true }
    }

    suspend fun getMostLikedPost(): Result<PostData> = safeApiCall {
        postApi.getMostLikedPost().toResult().map { it.toDomain() }
    }

    suspend fun getHashtagPosts(hashtag: String, page: Int = 1, size: Int = 10): Result<PageData> = safeApiCall {
        postApi.getHashtagPosts(page = page, size = size, hashtag = hashtag).toResult().map { springPage ->
            val posts = springPage.content.map { it.toDomain() }
            PageData(
                content = posts,
                nextCursor = posts.lastOrNull()?.postId,
                hasNext = !springPage.last
            )
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
        maxSizeBytes: Long = 1_500_000,
        maxDimension: Int = 1280,
        quality: Int = 80
    ): File {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val originalBytes = inputStream.available()
        inputStream.close()

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

        val outputFile = File(context.cacheDir, "$outputFileName.jpg")
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
}
