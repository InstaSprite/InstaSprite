package com.instasprite.app.data.network.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.instasprite.app.data.database.AppDatabase
import com.instasprite.app.data.model.MutationType
import com.instasprite.app.data.model.OfflineMutationEntity
import com.instasprite.app.data.network.api.CommentApi
import com.instasprite.app.data.network.api.PostApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val database: AppDatabase,
    private val postApi: PostApi,
    private val commentApi: CommentApi
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val mutationDao = database.offlineMutationDao()
        val pendingMutations = mutationDao.getAllPending()

        if (pendingMutations.isEmpty()) return Result.success()

        var hasNetworkError = false

        for (mutation in pendingMutations) {
            try {
                Log.d("SyncWorker", "Processing mutation: ${mutation.type} payload: ${mutation.payloadJson}")
                processMutation(mutation)
                mutationDao.dequeue(mutation)
            } catch (e: IOException) {
                Log.w("SyncWorker", "Network error on mutation ${mutation.id}: ${e.message}")
                hasNetworkError = true
                break
            } catch (e: Exception) {
                Log.e("SyncWorker", "Failed to process mutation ${mutation.id}, dequeueing. Error: ${e.message}")
                mutationDao.dequeue(mutation)
            }
        }

        return if (hasNetworkError) Result.retry() else Result.success()
    }

    private suspend fun processMutation(mutation: OfflineMutationEntity) {
        when (mutation.type) {
            MutationType.LIKE_POST -> {
                val postId = mutation.payloadJson.toLongOrNull() ?: throw IllegalArgumentException("Invalid payload")
                val response = postApi.likePost(postId)
                if (!response.isSuccessful) {
                    throw Exception("API returned ${response.code()}")
                }
            }
            MutationType.UNLIKE_POST -> {
                val postId = mutation.payloadJson.toLongOrNull() ?: throw IllegalArgumentException("Invalid payload")
                val response = postApi.unlikePost(postId)
                if (!response.isSuccessful) {
                    throw Exception("API returned ${response.code()}")
                }
            }
            else -> {
                Log.w("SyncWorker", "Unknown mutation type: ${mutation.type}")
            }
        }
    }
}
