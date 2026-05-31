package com.instasprite.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.instasprite.app.data.model.PostRemoteKeys

@Dao
interface PostRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<PostRemoteKeys>)

    @Query("SELECT * FROM post_remote_keys WHERE postId = :postId")
    suspend fun remoteKeysPostId(postId: Long): PostRemoteKeys?

    @Query("DELETE FROM post_remote_keys")
    suspend fun clearRemoteKeys()
}
