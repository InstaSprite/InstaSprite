package com.instasprite.app.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.instasprite.app.domain.model.PageData
import com.instasprite.app.domain.model.PostData

class FeedPagingSource(
    private val fetchLogic: suspend (cursor: Long?) -> Result<PageData>,
) : PagingSource<Long, PostData>() {

    override fun getRefreshKey(state: PagingState<Long, PostData>): Long? {
        return null
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, PostData> {
        val currentCursor = params.key

        return try {
            val result = fetchLogic(currentCursor)

            result.fold(
                onSuccess = { pageData ->
                    val nextKey = if (!pageData.hasNext || pageData.content.isEmpty()) {
                        null
                    } else {
                        pageData.nextCursor
                    }

                    LoadResult.Page(
                        data = pageData.content,
                        prevKey = null,
                        nextKey = nextKey
                    )
                },
                onFailure = { error ->
                    LoadResult.Error(error)
                }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
