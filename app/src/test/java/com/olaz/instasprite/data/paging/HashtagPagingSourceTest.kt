package com.olaz.instasprite.data.paging

import androidx.paging.PagingSource
import com.olaz.instasprite.domain.model.MemberData
import com.olaz.instasprite.domain.model.PageData
import com.olaz.instasprite.domain.model.PostData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class HashtagPagingSourceTest {

    private fun createPostData(id: Long) = PostData(
        postId = id,
        postContent = "Post #$id",
        postUploadDate = LocalDateTime.now(),
        member = MemberData(memberId = id, memberUsername = "user$id", memberName = "User $id"),
        postCommentsCount = 0,
        postLikesCount = 0,
        postBookmarkFlag = false,
        postLikeFlag = false,
        commentOptionFlag = true,
        isFollowing = false
    )

    @Test
    fun `first page loads correctly with hasNext = true`() = runTest {
        val posts = (1L..10L).map { createPostData(it) }
        val pagingSource = HashtagPagingSource { page ->
            if (page == 1) Result.success(PageData(content = posts, nextCursor = null, hasNext = true))
            else Result.success(PageData(content = emptyList(), nextCursor = null, hasNext = false))
        }

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(10, page.data.size)
        assertNull(page.prevKey)   // First page has no prev
        assertEquals(2, page.nextKey)  // hasNext = true → nextKey = 2
    }

    @Test
    fun `last page has null nextKey`() = runTest {
        val posts = (1L..5L).map { createPostData(it) }
        val pagingSource = HashtagPagingSource { page ->
            Result.success(PageData(content = posts, nextCursor = null, hasNext = false))
        }

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(5, page.data.size)
        assertNull(page.nextKey)
    }

    @Test
    fun `empty content results in null nextKey`() = runTest {
        val pagingSource = HashtagPagingSource { page ->
            Result.success(PageData(content = emptyList(), nextCursor = null, hasNext = true))
        }

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertTrue(page.data.isEmpty())
        assertNull(page.nextKey)
    }

    @Test
    fun `second page has prevKey = 1`() = runTest {
        val posts = (11L..20L).map { createPostData(it) }
        val pagingSource = HashtagPagingSource { page ->
            Result.success(PageData(content = posts, nextCursor = null, hasNext = true))
        }

        val result = pagingSource.load(
            PagingSource.LoadParams.Append(key = 2, loadSize = 10, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.prevKey)
        assertEquals(3, page.nextKey)
    }

    @Test
    fun `fetch failure returns LoadResult Error`() = runTest {
        val pagingSource = HashtagPagingSource { page ->
            Result.failure(Exception("Network error"))
        }

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Error)
        val error = result as PagingSource.LoadResult.Error
        assertEquals("Network error", error.throwable.message)
    }

    @Test
    fun `exception in fetch logic returns LoadResult Error`() = runTest {
        val pagingSource = HashtagPagingSource { page ->
            throw RuntimeException("Unexpected crash")
        }

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Error)
    }
}
