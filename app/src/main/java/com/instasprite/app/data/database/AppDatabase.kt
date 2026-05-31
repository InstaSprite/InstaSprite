package com.instasprite.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.instasprite.app.data.model.ColorPaletteData
import com.instasprite.app.data.model.FeedPostCrossRef
import com.instasprite.app.data.model.IntListConverter
import com.instasprite.app.data.model.NotificationEntity
import com.instasprite.app.data.model.NotificationRemoteKeys
import com.instasprite.app.data.model.PostEntity
import com.instasprite.app.data.model.PostRemoteKeys
import com.instasprite.app.data.model.SpriteData
import com.instasprite.app.data.model.SpriteMetaData
import com.instasprite.app.data.model.UserEntity
import com.instasprite.app.data.model.UserProfileEntity
import com.instasprite.app.data.model.OfflineMutationEntity

@Database(
    entities = [
        SpriteData::class,
        SpriteMetaData::class,
        ColorPaletteData::class,
        UserProfileEntity::class,
        UserEntity::class,
        PostEntity::class,
        FeedPostCrossRef::class,
        PostRemoteKeys::class,
        NotificationEntity::class,
        NotificationRemoteKeys::class,
        OfflineMutationEntity::class
    ], version = 7, exportSchema = false
)
@TypeConverters(
    value = [
        IntListConverter::class,
        ColorPaletteConverters::class,
        AppTypeConverters::class
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun spriteDataDao(): SpriteDataDao
    abstract fun spriteMetaDataDao(): SpriteMetaDataDao
    abstract fun colorPaletteDao(): ColorPaletteDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao
    abstract fun postRemoteKeysDao(): PostRemoteKeysDao
    abstract fun notificationDao(): NotificationDao
    abstract fun notificationRemoteKeysDao(): NotificationRemoteKeysDao
    abstract fun offlineMutationDao(): OfflineMutationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `palette_data` ADD COLUMN `isFavorite` INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `current_user_profile` (`memberId` INTEGER NOT NULL, `profileDto` TEXT NOT NULL, PRIMARY KEY(`memberId`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `posts` (`postId` INTEGER NOT NULL, `postDto` TEXT NOT NULL, `pageFilter` TEXT NOT NULL, PRIMARY KEY(`postId`, `pageFilter`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `post_remote_keys` (`postId` INTEGER NOT NULL, `prevKey` INTEGER, `nextKey` INTEGER, PRIMARY KEY(`postId`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (`id` TEXT NOT NULL, `notificationDto` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `notification_remote_keys` (`id` TEXT NOT NULL, `prevKey` INTEGER, `nextKey` INTEGER, PRIMARY KEY(`id`))")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `posts`")
                db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`memberId` INTEGER NOT NULL, `username` TEXT NOT NULL, `name` TEXT NOT NULL, `avatarUrl` TEXT, PRIMARY KEY(`memberId`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `posts` (`postId` INTEGER NOT NULL, `authorId` INTEGER NOT NULL, `postContent` TEXT, `postUploadDate` TEXT, `postCommentsCount` INTEGER NOT NULL, `postLikesCount` INTEGER NOT NULL, `postBookmarkFlag` INTEGER NOT NULL, `postLikeFlag` INTEGER NOT NULL, `commentOptionFlag` INTEGER NOT NULL, `likeOptionFlag` INTEGER NOT NULL, `isFollowing` INTEGER, `followingMemberUsernameLikedPost` TEXT, `mentionsOfContent` TEXT, `hashtags` TEXT, `postImages` TEXT, `recentComments` TEXT, PRIMARY KEY(`postId`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `feed_post_cross_ref` (`postId` INTEGER NOT NULL, `pageFilter` TEXT NOT NULL, PRIMARY KEY(`postId`, `pageFilter`))")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `notifications`")
                db.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (`id` TEXT NOT NULL, `groupKey` TEXT NOT NULL, `type` TEXT NOT NULL, `relatedEntityId` TEXT, `actorCount` INTEGER NOT NULL, `isRead` INTEGER NOT NULL, `updatedAt` TEXT, `recentActors` TEXT NOT NULL, `title` TEXT, `body` TEXT, PRIMARY KEY(`id`))")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `current_user_profile`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `current_user_profile` (
                        `memberId` INTEGER NOT NULL,
                        `memberUsername` TEXT NOT NULL,
                        `memberName` TEXT NOT NULL,
                        `memberImage` TEXT,
                        `memberImageUrl` TEXT,
                        `memberIntroduce` TEXT,
                        `memberPostsCount` INTEGER NOT NULL,
                        `memberFollowingsCount` INTEGER NOT NULL,
                        `memberFollowersCount` INTEGER NOT NULL,
                        `followingMemberFollow` TEXT NOT NULL,
                        `followingMemberFollowCount` INTEGER NOT NULL,
                        `blocking` INTEGER NOT NULL,
                        `following` INTEGER NOT NULL,
                        `me` INTEGER NOT NULL,
                        `follower` INTEGER NOT NULL,
                        `blocked` INTEGER NOT NULL,
                        `verifiedEmail` INTEGER NOT NULL,
                        `hasPassword` INTEGER NOT NULL,
                        PRIMARY KEY(`memberId`)
                    )
                """.trimIndent()
                )
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `offline_mutations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `payloadJson` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `retryCount` INTEGER NOT NULL)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "instasprite_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
