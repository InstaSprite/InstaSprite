package com.instasprite.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.paging.map
import coil3.decode.DecodeUtils.calculateInSampleSize
import coil3.size.Scale
import com.instasprite.app.data.network.S3UploadClient
import com.instasprite.app.data.network.api.PostApi
import com.instasprite.app.data.network.model.CommitPostRequestDto
import com.instasprite.app.data.network.model.UploadInitRequestDto
import com.instasprite.app.data.network.model.toDomain
import com.instasprite.app.data.network.safeApiCall
import com.instasprite.app.data.network.toResult
import com.instasprite.app.data.network.toResultMessage
import com.instasprite.app.data.network.toResultUnit
import com.instasprite.app.domain.model.PageData
import com.instasprite.app.domain.model.PostData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

class PostRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val postApi: PostApi,
    private val s3UploadClient: S3UploadClient,
    private val database: com.instasprite.app.data.database.AppDatabase,
    private val syncManager: com.instasprite.app.data.network.sync.SyncManager
) {

    fun getPagedPosts(filter: com.instasprite.app.ui.social.feed.PostFilter): kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<PostData>> {
        @OptIn(androidx.paging.ExperimentalPagingApi::class)
        return androidx.paging.Pager(
            config = androidx.paging.PagingConfig(pageSize = 10, enablePlaceholders = false),
            remoteMediator = com.instasprite.app.data.paging.FeedRemoteMediator(postApi, database, filter),
            pagingSourceFactory = { database.postDao().pagingSource(filter.name) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    suspend fun getPost(postId: Long): Result<PostData> = safeApiCall {
        postApi.getPost(postId).toResult().map { it.toDomain() }
    }

    suspend fun createPost(
        content: String,
        images: List<Uri>,
        altTexts: List<String>,
        hashtags: List<String>,
        commentFlag: Boolean,
    ): Result<Long> = safeApiCall {
        val fileInfos = images.map { uri ->
            val mimeType = context.contentResolver.getType(uri)
                ?: mimeTypeFromExtension(uri)
            
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                android.graphics.BitmapFactory.decodeStream(stream, null, options)
            }
            val width = if (options.outWidth > 0) options.outWidth else 1080
            val height = if (options.outHeight > 0) options.outHeight else 1080

            // Extract dominant color using a downscaled thumbnail
            var dominantColorHex: String? = null
            try {
                val scaleOptions = BitmapFactory.Options().apply {
                    inSampleSize = 1.coerceAtLeast((width / 100).coerceAtMost(height / 100))
                }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val thumbnail = BitmapFactory.decodeStream(stream, null, scaleOptions)
                    if (thumbnail != null) {
                        val palette = androidx.palette.graphics.Palette.from(thumbnail).generate()
                        palette.dominantSwatch?.let { swatch ->
                            dominantColorHex = String.format("#%06X", (0xFFFFFF and swatch.rgb))
                        }
                        thumbnail.recycle()
                    }
                }
            } catch (e: Exception) {
            }

            UploadInitRequestDto.FileInfo(
                contentType = mimeType,
                width = width,
                height = height,
                dominantColor = dominantColorHex
            )
        }

        val initData = postApi.uploadInit(UploadInitRequestDto(files = fileInfos))
            .toResult()
            .getOrThrow()

        for ((index, upload) in initData.uploads.withIndex()) {
            val file = compressImageIfNeeded(
                context = context,
                uri = images[index],
                outputFileName = "post_image_$index"
            )
            val presignedUrl = if (upload.url.startsWith("http")) upload.url else "${com.instasprite.app.utils.Constants.BASE_URL}${upload.url}"
            s3UploadClient.uploadFile(
                presignedUrl = presignedUrl,
                file = file,
                contentType = fileInfos[index].contentType
            ).getOrThrow()
        }

        val commitRequest = CommitPostRequestDto(
            postId = initData.postId,
            content = content,
            commentFlag = commentFlag,
            altTexts = altTexts,
            hashtags = hashtags
        )
        postApi.commitPost(commitRequest).toResult().map { it.postId }
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

    suspend fun likePost(postId: Long): Result<String> {
        val post = database.postDao().getPostById(postId)
        if (post != null) {
            val newLikeCount = post.postLikesCount + 1
            database.postDao().updateLikeState(postId, true, newLikeCount)
        }
        syncManager.enqueueMutation(com.instasprite.app.data.model.MutationType.LIKE_POST, postId.toString())
        return Result.success("Post liked offline")
    }

    suspend fun unlikePost(postId: Long): Result<String> {
        val post = database.postDao().getPostById(postId)
        if (post != null) {
            val newLikeCount = if (post.postLikesCount > 0) post.postLikesCount - 1 else 0
            database.postDao().updateLikeState(postId, false, newLikeCount)
        }
        syncManager.enqueueMutation(com.instasprite.app.data.model.MutationType.UNLIKE_POST, postId.toString())
        return Result.success("Post unliked offline")
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

        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val (format, ext) = when {
            mimeType.contains("png") -> Bitmap.CompressFormat.PNG to "png"
            else -> Bitmap.CompressFormat.JPEG to "jpg"
        }

        val outputFile = File(context.cacheDir, "$outputFileName.$ext")
        FileOutputStream(outputFile).use {
            scaledBitmap.compress(format, quality, it)
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

    private fun mimeTypeFromExtension(uri: Uri): String {
        return when (getFileExtension(uri)) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
    }
}
