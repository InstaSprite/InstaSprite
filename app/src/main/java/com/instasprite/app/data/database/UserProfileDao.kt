package com.instasprite.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.instasprite.app.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userProfile: UserProfileEntity)

    @Query("SELECT * FROM current_user_profile LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM current_user_profile LIMIT 1")
    suspend fun getCurrentUser(): UserProfileEntity?

    @Query("DELETE FROM current_user_profile")
    suspend fun clear()
}
