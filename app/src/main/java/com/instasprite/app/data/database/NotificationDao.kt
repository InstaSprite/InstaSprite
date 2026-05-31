package com.instasprite.app.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.instasprite.app.data.model.NotificationEntity

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Query("SELECT * FROM notifications ORDER BY rowid ASC")
    fun pagingSource(): PagingSource<Int, NotificationEntity>

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}
