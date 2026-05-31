package com.instasprite.app.data.network.sync

import android.content.Context
import androidx.work.*
import com.instasprite.app.data.database.AppDatabase
import com.instasprite.app.data.model.MutationType
import com.instasprite.app.data.model.OfflineMutationEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface SyncManager {
    suspend fun enqueueMutation(type: MutationType, payloadJson: String)
}

@Singleton
class SyncManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) : SyncManager {

    override suspend fun enqueueMutation(type: MutationType, payloadJson: String) {
        withContext(Dispatchers.IO) {
            val dao = database.offlineMutationDao()
            dao.enqueue(
                OfflineMutationEntity(
                    type = type,
                    payloadJson = payloadJson
                )
            )

            // Trigger background sync
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "OfflineSyncWork",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                syncRequest
            )
        }
    }
}
