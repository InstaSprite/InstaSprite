package com.instasprite.app.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.instasprite.app.domain.model.PageData
import com.instasprite.app.domain.model.PostData

class HashtagPagingSource(
    private val fetchLogic: suspend (page: Int) -> Result<PageData>,
) : PagingSource<Int, PostData>() {

    override fun getRefreshKey(state: PagingState<Int, PostData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PostData> {
        val currentPage = params.key ?: 1

        return try {
            val result = fetchLogic(currentPage)

            result.fold(
                onSuccess = { pageData ->
                    val nextKey = if (!pageData.hasNext || pageData.content.isEmpty()) {
                        null
                    } else {
                        currentPage + 1
                    }

                    LoadResult.Page(
                        data = pageData.content,
                        prevKey = if (currentPage == 1) null else currentPage - 1,
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
