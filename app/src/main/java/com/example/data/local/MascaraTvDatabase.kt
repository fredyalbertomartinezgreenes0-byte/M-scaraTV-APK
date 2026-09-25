package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val durationFormatted: String,
    val durationSeconds: Int,
    val viewsCount: Long,
    val publishedTimeAgo: String,
    val channelId: String,
    val channelName: String,
    val channelAvatar: String,
    val isVerified: Boolean,
    val category: String,
    val tagsJson: String,
    val likesCount: Long,
    val dislikesCount: Long,
    val isLiked: Boolean,
    val isDisliked: Boolean,
    val isSaved: Boolean,
    val isDownloaded: Boolean,
    val isShort: Boolean,
    val allowDownload: Boolean,
    val visibility: String,
    val localFilePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val handle: String,
    val description: String,
    val avatarUrl: String,
    val bannerUrl: String,
    val subscribersCount: Long,
    val isSubscribed: Boolean,
    val isVerified: Boolean,
    val isUserOwned: Boolean,
    val videoCount: Int
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val videoId: String,
    val userName: String,
    val userAvatar: String,
    val text: String,
    val timeAgo: String,
    val likesCount: Int,
    val isLiked: Boolean,
    val isOwner: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val videoId: String,
    val watchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_videos")
data class SavedVideoEntity(
    @PrimaryKey val videoId: String,
    val listType: String = "WATCH_LATER", // WATCH_LATER, FAVORITES
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val channelId: String,
    val subscribedAt: Long = System.currentTimeMillis()
)

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY timestamp DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isShort = 0 ORDER BY timestamp DESC")
    fun getStandardVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isShort = 1 ORDER BY timestamp DESC")
    fun getShortsVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    fun getVideoById(id: String): Flow<VideoEntity?>

    @Query("SELECT * FROM videos WHERE channelId = :channelId ORDER BY timestamp DESC")
    fun getVideosByChannel(channelId: String): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Query("UPDATE videos SET likesCount = :likes, isLiked = :isLiked, isDisliked = 0 WHERE id = :id")
    suspend fun updateLike(id: String, likes: Long, isLiked: Boolean)

    @Query("UPDATE videos SET dislikesCount = :dislikes, isDisliked = :isDisliked, isLiked = 0 WHERE id = :id")
    suspend fun updateDislike(id: String, dislikes: Long, isDisliked: Boolean)

    @Query("UPDATE videos SET isSaved = :isSaved WHERE id = :id")
    suspend fun updateSaved(id: String, isSaved: Boolean)

    @Query("UPDATE videos SET isDownloaded = :isDownloaded WHERE id = :id")
    suspend fun updateDownloaded(id: String, isDownloaded: Boolean)

    @Query("UPDATE videos SET viewsCount = viewsCount + 1 WHERE id = :id")
    suspend fun incrementViews(id: String)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideo(id: String)
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    fun getChannelById(id: String): Flow<ChannelEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Query("UPDATE channels SET isSubscribed = :subscribed, subscribersCount = subscribersCount + :delta WHERE id = :id")
    suspend fun updateSubscription(id: String, subscribed: Boolean, delta: Long)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY timestamp DESC")
    fun getCommentsForVideo(videoId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("UPDATE comments SET likesCount = likesCount + :delta, isLiked = :isLiked WHERE id = :commentId")
    suspend fun updateCommentLike(commentId: String, delta: Int, isLiked: Boolean)

    @Query("DELETE FROM comments WHERE id = :id")
    suspend fun deleteComment(id: String)
}

@Dao
interface HistoryAndSavedDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToHistory(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE videoId = :videoId")
    suspend fun removeFromHistory(videoId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()

    @Query("SELECT * FROM saved_videos ORDER BY savedAt DESC")
    fun getSavedVideos(): Flow<List<SavedVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVideo(item: SavedVideoEntity)

    @Query("DELETE FROM saved_videos WHERE videoId = :videoId")
    suspend fun unsaveVideo(videoId: String)
}

@Database(
    entities = [
        VideoEntity::class,
        ChannelEntity::class,
        CommentEntity::class,
        WatchHistoryEntity::class,
        SavedVideoEntity::class,
        SubscriptionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MascaraTvDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun channelDao(): ChannelDao
    abstract fun commentDao(): CommentDao
    abstract fun historyAndSavedDao(): HistoryAndSavedDao

    companion object {
        @Volatile
        private var INSTANCE: MascaraTvDatabase? = null

        fun getDatabase(context: Context): MascaraTvDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MascaraTvDatabase::class.java,
                    "mascara_tv_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
