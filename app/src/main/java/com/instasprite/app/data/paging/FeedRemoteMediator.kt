package com.instasprite.app.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.instasprite.app.data.database.AppDatabase
import com.instasprite.app.data.model.FeedPostCrossRef
import com.instasprite.app.data.model.PostRemoteKeys
import com.instasprite.app.data.model.PostWithAuthor
import com.instasprite.app.data.network.api.PostApi
import com.instasprite.app.data.network.getBodyOrError
import com.instasprite.app.di.RetrofitModule
import com.instasprite.app.data.network.model.toUserEntity
import com.instasprite.app.data.network.model.toPostEntity
import com.instasprite.app.ui.social.feed.PostFilter

@OptIn(ExperimentalPagingApi::class)
class FeedRemoteMediator(
    private val postApi: PostApi,
    private val database: AppDatabase,
    private val filter: PostFilter
) : RemoteMediator<Int, PostWithAuthor>() {

    private val postDao = database.postDao()
    private val userDao = database.userDao()
    private val remoteKeysDao = database.postRemoteKeysDao()
    private val filterString = filter.name

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostWithAuthor>
    ): MediatorResult {
        return try {
            val cursor = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    // If remoteKeys is null, that means the refresh result is not in the database yet.
                    if (remoteKeys == null || remoteKeys.nextKey == null) {
                        return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    }
                    remoteKeys.nextKey
                }
            }

            val response = when (filter) {
                PostFilter.Recent -> postApi.getRecentPostsPage(cursor)
                PostFilter.Follow -> postApi.getPostPage(cursor)
            }
            
            val body = response.getBodyOrError(RetrofitModule.gson)
            if (body == null || body.status != 200 || body.data == null) {
                return MediatorResult.Error(Exception(body?.message ?: "Unknown Error"))
            }

            val pageData = body.data
            val posts = pageData.content
            val endOfPaginationReached = !pageData.hasNext

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeysDao.clearRemoteKeys()
                    postDao.clearByFilter(filterString)
                }

                val keys = posts.map { post ->
                    PostRemoteKeys(
                        postId = post.postId,
                        prevKey = null, // Since we only append, prevKey isn't strictly needed for our API structure
                        nextKey = pageData.nextCursor
                    )
                }

                val users = posts.mapNotNull { it.member?.toUserEntity() }
                val postEntities = posts.map { it.toPostEntity() }
                val crossRefs = posts.map { FeedPostCrossRef(it.postId, filterString) }

                remoteKeysDao.insertAll(keys)
                userDao.insertAll(users)
                postDao.insertAll(postEntities)
                postDao.insertFeedCrossRefs(crossRefs)
            }
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, PostWithAuthor>): PostRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { item ->
                remoteKeysDao.remoteKeysPostId(item.post.postId)
            }
    }
}
