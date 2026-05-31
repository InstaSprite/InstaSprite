package com.instasprite.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.instasprite.app.data.model.NotificationRemoteKeys

@Dao
interface NotificationRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<NotificationRemoteKeys>)

    @Query("SELECT * FROM notification_remote_keys WHERE id = :id")
    suspend fun remoteKeysId(id: String): NotificationRemoteKeys?

    @Query("DELETE FROM notification_remote_keys")
    suspend fun clearRemoteKeys()
}
