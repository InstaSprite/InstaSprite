package com.instasprite.app.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.instasprite.app.data.model.FeedPostCrossRef
import com.instasprite.app.data.model.PostEntity
import com.instasprite.app.data.model.PostWithAuthor

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFeedCrossRefs(refs: List<FeedPostCrossRef>)

    @Transaction
    @Query("SELECT posts.* FROM posts INNER JOIN feed_post_cross_ref ON posts.postId = feed_post_cross_ref.postId WHERE feed_post_cross_ref.pageFilter = :filter ORDER BY feed_post_cross_ref.rowid ASC")
    fun pagingSource(filter: String): PagingSource<Int, PostWithAuthor>

    @Transaction
    @Query("SELECT posts.* FROM posts INNER JOIN feed_post_cross_ref ON posts.postId = feed_post_cross_ref.postId WHERE feed_post_cross_ref.pageFilter = :filter ORDER BY feed_post_cross_ref.rowid ASC LIMIT 10")
    suspend fun getRecentPosts(filter: String): List<PostWithAuthor>

    @Query("DELETE FROM feed_post_cross_ref WHERE pageFilter = :filter")
    suspend fun clearByFilter(filter: String)

    @Query("DELETE FROM posts")
    suspend fun clearAll()

    @Query("SELECT * FROM posts WHERE postId = :postId")
    suspend fun getPostById(postId: Long): PostEntity?

    @Query("UPDATE posts SET postLikeFlag = :isLiked, postLikesCount = :likeCount WHERE postId = :postId")
    suspend fun updateLikeState(postId: Long, isLiked: Boolean, likeCount: Long)
}
