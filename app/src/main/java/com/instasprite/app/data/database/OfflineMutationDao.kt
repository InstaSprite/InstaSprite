package com.instasprite.app.data.database

import androidx.room.*
import com.instasprite.app.data.model.OfflineMutationEntity

@Dao
interface OfflineMutationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(mutation: OfflineMutationEntity): Long

    @Query("SELECT * FROM offline_mutations ORDER BY createdAt ASC")
    suspend fun getAllPending(): List<OfflineMutationEntity>

    @Delete
    suspend fun dequeue(mutation: OfflineMutationEntity)

    @Update
    suspend fun update(mutation: OfflineMutationEntity)

    @Query("DELETE FROM offline_mutations")
    suspend fun clearAll()
}
