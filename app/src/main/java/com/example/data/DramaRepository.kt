package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DramaRepository(private val dramaDao: DramaDao) {

    val allHistory: Flow<List<WatchHistoryEntity>> = dramaDao.getAllHistory()
    val recentHistory: Flow<List<WatchHistoryEntity>> = dramaDao.getRecentHistory(10)
    val allBookmarks: Flow<List<BookmarkEntity>> = dramaDao.getAllBookmarks()

    val userWallet: Flow<UserWalletEntity> = dramaDao.getUserWallet().map {
        it ?: UserWalletEntity(userId = "primary_user", coins = 100, isVip = false)
    }

    val adminConfig: Flow<AdminConfigEntity> = dramaDao.getAdminConfig().map {
        it ?: AdminConfigEntity()
    }

    fun isBookmarked(dramaId: String): Flow<Boolean> = dramaDao.isBookmarked(dramaId)

    fun getUnlockedEpisodes(dramaId: String): Flow<List<Int>> = dramaDao.getUnlockedEpisodes(dramaId)

    suspend fun recordWatch(
        dramaId: String,
        title: String,
        url: String,
        episodeName: String,
        coverUrl: String = ""
    ) {
        val historyItem = WatchHistoryEntity(
            dramaId = dramaId,
            title = title,
            url = url,
            episodeName = episodeName,
            coverUrl = coverUrl,
            watchedAt = System.currentTimeMillis()
        )
        dramaDao.insertHistory(historyItem)
    }

    suspend fun removeHistory(id: Long) {
        dramaDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        dramaDao.clearAllHistory()
    }

    suspend fun toggleBookmark(
        dramaId: String,
        title: String,
        url: String,
        description: String = "",
        coverUrl: String = "",
        category: String = "AI Drama",
        currentlyBookmarked: Boolean
    ) {
        if (currentlyBookmarked) {
            dramaDao.deleteBookmark(dramaId)
        } else {
            dramaDao.insertBookmark(
                BookmarkEntity(
                    dramaId = dramaId,
                    title = title,
                    url = url,
                    description = description,
                    coverUrl = coverUrl,
                    category = category,
                    savedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // Monetization & Wallet
    suspend fun addCoins(amount: Int) {
        val currentWallet = dramaDao.getUserWallet()
        // We will read once or update
    }

    suspend fun updateWallet(wallet: UserWalletEntity) {
        dramaDao.saveUserWallet(wallet)
    }

    suspend fun unlockEpisode(dramaId: String, episodeIndex: Int) {
        dramaDao.unlockEpisode(
            UnlockedEpisodeEntity(
                dramaId = dramaId,
                episodeIndex = episodeIndex,
                unlockedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun resetAllUnlocks() {
        dramaDao.resetAllUnlockedEpisodes()
    }

    suspend fun updateAdminConfig(config: AdminConfigEntity) {
        dramaDao.saveAdminConfig(config)
    }
}
