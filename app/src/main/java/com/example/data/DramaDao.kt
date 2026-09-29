package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DramaDao {

    // History queries
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 10): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: WatchHistoryEntity): Long

    @Query("DELETE FROM watch_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()

    // Bookmark queries
    @Query("SELECT * FROM bookmarks ORDER BY savedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE dramaId = :dramaId LIMIT 1)")
    fun isBookmarked(dramaId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(item: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE dramaId = :dramaId")
    suspend fun deleteBookmark(dramaId: String)

    // User Wallet & VIP
    @Query("SELECT * FROM user_wallet WHERE userId = :userId LIMIT 1")
    fun getUserWallet(userId: String = "primary_user"): Flow<UserWalletEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserWallet(wallet: UserWalletEntity)

    // Unlocked Episodes
    @Query("SELECT episodeIndex FROM unlocked_episodes WHERE dramaId = :dramaId")
    fun getUnlockedEpisodes(dramaId: String): Flow<List<Int>>

    @Query("SELECT EXISTS(SELECT 1 FROM unlocked_episodes WHERE dramaId = :dramaId AND episodeIndex = :episodeIndex LIMIT 1)")
    suspend fun isEpisodeUnlocked(dramaId: String, episodeIndex: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun unlockEpisode(item: UnlockedEpisodeEntity)

    @Query("DELETE FROM unlocked_episodes")
    suspend fun resetAllUnlockedEpisodes()

    // Admin Config
    @Query("SELECT * FROM admin_config WHERE id = 1 LIMIT 1")
    fun getAdminConfig(): Flow<AdminConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdminConfig(config: AdminConfigEntity)
}
