package com.instasprite.app.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class S3UploadClient @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun uploadFile(
        presignedUrl: String,
        file: File,
        contentType: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null

        repeat(2) { attempt ->
            try {
                val body = file.asRequestBody(contentType.toMediaType())
                val request = Request.Builder()
                    .url(presignedUrl)
                    .put(body)
                    .header("Content-Type", contentType)
                    .build()

                val response = client.newCall(request).execute()
                response.use {
                    if (it.isSuccessful) {
                        return@withContext Result.success(Unit)
                    }
                    lastException = Exception("S3 upload failed (attempt ${attempt + 1}): ${it.code}")
                }
            } catch (e: Exception) {
                lastException = e
            }
        }

        Result.failure(lastException ?: Exception("S3 upload failed"))
    }
}
