package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dramaId: String,
    val title: String,
    val url: String,
    val episodeName: String = "Episode 1",
    val coverUrl: String = "",
    val watchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val dramaId: String,
    val title: String,
    val url: String,
    val description: String = "",
    val coverUrl: String = "",
    val category: String = "AI Drama",
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_wallet")
data class UserWalletEntity(
    @PrimaryKey
    val userId: String = "primary_user",
    val coins: Int = 100,
    val isVip: Boolean = false,
    val isLifetimePremium: Boolean = false,
    val orderId: String = "",
    val purchaseToken: String = "",
    val purchaseTime: Long = 0L
)

@Entity(tableName = "unlocked_episodes", primaryKeys = ["dramaId", "episodeIndex"])
data class UnlockedEpisodeEntity(
    val dramaId: String,
    val episodeIndex: Int,
    val unlockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_config")
data class AdminConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val interstitialFrequency: Int = 1, // Every N episodes played
    val rewardedAdCoins: Int = 50, // Coins awarded per rewarded ad
    val episodeCoinCost: Int = 20, // Coins to unlock 1 episode
    val freeEpisodesPerDrama: Int = 3, // Episodes 1..N are free, N+1 requires unlock
    val adDurationSeconds: Int = 5,
    val allowSkipAfterSeconds: Int = 3
)
