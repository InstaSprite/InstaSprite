package com.instasprite.app.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.instasprite.app.data.database.AppDatabase
import com.instasprite.app.data.model.NotificationEntity
import com.instasprite.app.data.model.NotificationRemoteKeys
import com.instasprite.app.data.network.api.NotificationApi
import com.instasprite.app.data.network.getBodyOrError
import com.instasprite.app.di.RetrofitModule

@OptIn(ExperimentalPagingApi::class)
class NotificationRemoteMediator(
    private val notificationApi: NotificationApi,
    private val database: AppDatabase
) : RemoteMediator<Int, NotificationEntity>() {

    private val notificationDao = database.notificationDao()
    private val remoteKeysDao = database.notificationRemoteKeysDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NotificationEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    if (remoteKeys == null || remoteKeys.nextKey == null) {
                        return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    }
                    remoteKeys.nextKey
                }
            }

            val response = notificationApi.getGroupedNotifications(page = page, size = 20)
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body == null || body.status != 200 || body.data == null) {
                return MediatorResult.Error(Exception(body?.message ?: "Unknown Error"))
            }

            val pageDto = body.data
            val notifications = pageDto.content
            val endOfPaginationReached = pageDto.last

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeysDao.clearRemoteKeys()
                    notificationDao.clearAll()
                }

                val prevKey = if (page == 0) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                val keys = notifications.map {
                    NotificationRemoteKeys(
                        id = it.id,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }

                val entities = notifications.map {
                    NotificationEntity(
                        id = it.id,
                        groupKey = it.groupKey,
                        type = it.type,
                        relatedEntityId = it.relatedEntityId,
                        actorCount = it.actorCount,
                        isRead = it.isRead,
                        updatedAt = it.updatedAt,
                        recentActors = it.recentActors,
                        title = it.title,
                        body = it.body
                    )
                }

                remoteKeysDao.insertAll(keys)
                notificationDao.insertAll(entities)
            }
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, NotificationEntity>): NotificationRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { notification ->
                remoteKeysDao.remoteKeysId(notification.id)
            }
    }
}
