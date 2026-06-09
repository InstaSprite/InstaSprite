package com.instasprite.app.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.instasprite.app.data.network.api.PostApi
import com.instasprite.app.data.network.model.toFollowerUser
import com.instasprite.app.ui.social.profile.contract.FollowerUser

class PostLikesPagingSource(
    private val api: PostApi,
    private val postId: Long
) : PagingSource<Int, FollowerUser>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FollowerUser> {
        val page = params.key ?: 1
        return try {
            val response = api.getMembersLikedPost(
                postId = postId,
                page = page,
                size = params.loadSize
            )

            if (response.isSuccessful && response.body()?.status == 200) {
                val pagedData = response.body()?.data
                val followers = pagedData?.content?.map { it.toFollowerUser() } ?: emptyList()
                val nextKey = if (pagedData?.last == false) page + 1 else null
                
                LoadResult.Page(
                    data = followers,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = nextKey
                )
            } else {
                LoadResult.Error(Exception("Failed to load post likes"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, FollowerUser>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
