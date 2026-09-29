package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.billing.BillingManager
import com.example.billing.BillingUiState
import com.example.data.AdminConfigEntity
import com.example.data.AppDatabase
import com.example.data.BookmarkEntity
import com.example.data.DramaCatalog
import com.example.data.DramaItem
import com.example.data.DramaRepository
import com.example.data.UserWalletEntity
import com.example.data.WatchHistoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavTab {
    DISCOVER,
    CINEMA,
    SAVED,
    HISTORY,
    ADMIN
}

data class PlayerState(
    val currentDrama: DramaItem = DramaCatalog.TARGET_FEATURED_DRAMA,
    val currentEpisodeIndex: Int = 1,
    val activeUrl: String = DramaCatalog.TARGET_FEATURED_DRAMA.url,
    val pageTitle: String = DramaCatalog.TARGET_FEATURED_DRAMA.title,
    val currentEpisodeName: String = "Episode 1",
    val loadingProgress: Int = 0,
    val isLoading: Boolean = false,
    val blockedAdsCount: Int = 0,
    val isTheaterMaximized: Boolean = false
)

data class MonetizationUiState(
    val showInterstitialAd: Boolean = false,
    val showRewardedAd: Boolean = false,
    val rewardedCoinsTarget: Int = 50,
    val rewardedEpisodeIndexTarget: Int? = null,
    val showUnlockDialog: Boolean = false,
    val pendingDramaToUnlock: DramaItem? = null,
    val pendingEpisodeIndexToUnlock: Int = 1,
    val showVipDialog: Boolean = false,
    val showLifetimePremiumScreen: Boolean = false,
    val episodesPlayedCounter: Int = 0
)

class DramaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DramaRepository
    val billingManager: BillingManager

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DramaRepository(db.dramaDao())

        billingManager = BillingManager(
            context = application,
            onPurchaseVerifiedAndAcknowledged = { orderId, token, time ->
                viewModelScope.launch {
                    val current = repository.userWallet
                    // Update user wallet to permanent Lifetime Premium & VIP
                    repository.updateWallet(
                        UserWalletEntity(
                            userId = "primary_user",
                            coins = 500, // Bonus starter coins with lifetime
                            isVip = true,
                            isLifetimePremium = true,
                            orderId = orderId,
                            purchaseToken = token,
                            purchaseTime = time
                        )
                    )
                }
            }
        )
        // Initiate connection to Google Play Billing
        billingManager.startBillingConnection()
    }

    val billingUiState: StateFlow<BillingUiState> = billingManager.billingState

    private val _selectedTab = MutableStateFlow(NavTab.DISCOVER)
    val selectedTab: StateFlow<NavTab> = _selectedTab.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _monetizationState = MutableStateFlow(MonetizationUiState())
    val monetizationState: StateFlow<MonetizationUiState> = _monetizationState.asStateFlow()

    val userWallet: StateFlow<UserWalletEntity> = repository.userWallet
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserWalletEntity()
        )

    val adminConfig: StateFlow<AdminConfigEntity> = repository.adminConfig
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AdminConfigEntity()
        )

    val currentUnlockedEpisodes: StateFlow<List<Int>> = _playerState
        .flatMapLatest { state -> repository.getUnlockedEpisodes(state.currentDrama.id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentHistory: StateFlow<List<WatchHistoryEntity>> = repository.recentHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isCurrentBookmarked: StateFlow<Boolean> = _playerState
        .flatMapLatest { state -> repository.isBookmarked(state.currentDrama.id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun selectTab(tab: NavTab) {
        _selectedTab.value = tab
    }

    /**
     * Primary Playback entry with Ad & Paywall rules check:
     */
    fun requestPlayEpisode(drama: DramaItem, episodeIndex: Int = 1) {
        val isVip = userWallet.value.isVip || userWallet.value.isLifetimePremium
        val freeLimit = adminConfig.value.freeEpisodesPerDrama
        val isAlreadyUnlocked = episodeIndex <= freeLimit || currentUnlockedEpisodes.value.contains(episodeIndex)

        if (isVip || isAlreadyUnlocked) {
            // Episode is accessible, check if Interstitial Ad should trigger for Free Users
            if (!isVip) {
                val currentCounter = _monetizationState.value.episodesPlayedCounter + 1
                val freq = adminConfig.value.interstitialFrequency
                if (currentCounter >= freq) {
                    // Trigger Interstitial Ad
                    _monetizationState.value = _monetizationState.value.copy(
                        showInterstitialAd = true,
                        episodesPlayedCounter = 0
                    )
                    // Queue drama to start after ad finishes
                    executePlayback(drama, episodeIndex)
                    return
                } else {
                    _monetizationState.value = _monetizationState.value.copy(
                        episodesPlayedCounter = currentCounter
                    )
                }
            }
            executePlayback(drama, episodeIndex)
        } else {
            // Episode is locked for free user -> show Unlock dialog
            _monetizationState.value = _monetizationState.value.copy(
                showUnlockDialog = true,
                pendingDramaToUnlock = drama,
                pendingEpisodeIndexToUnlock = episodeIndex
            )
        }
    }

    private fun executePlayback(drama: DramaItem, episodeIndex: Int) {
        val episodeUrl = if (episodeIndex == 1) {
            drama.url
        } else {
            "${drama.url}#ep$episodeIndex"
        }

        _playerState.value = _playerState.value.copy(
            currentDrama = drama,
            currentEpisodeIndex = episodeIndex,
            activeUrl = episodeUrl,
            pageTitle = "${drama.title} - Ep $episodeIndex",
            currentEpisodeName = "Episode $episodeIndex",
            loadingProgress = 10,
            isLoading = true
        )
        _selectedTab.value = NavTab.CINEMA
        recordWatchHistory(drama.id, "${drama.title} - Ep $episodeIndex", episodeUrl, "Episode $episodeIndex", drama.coverUrl)
    }

    fun onInterstitialAdFinished() {
        _monetizationState.value = _monetizationState.value.copy(showInterstitialAd = false)
    }

    fun dismissUnlockDialog() {
        _monetizationState.value = _monetizationState.value.copy(showUnlockDialog = false)
    }

    fun unlockEpisodeWithCoins(dramaId: String, episodeIndex: Int) {
        val currentCoins = userWallet.value.coins
        val cost = adminConfig.value.episodeCoinCost
        if (currentCoins >= cost) {
            viewModelScope.launch {
                repository.updateWallet(userWallet.value.copy(coins = currentCoins - cost))
                repository.unlockEpisode(dramaId, episodeIndex)
                _monetizationState.value = _monetizationState.value.copy(showUnlockDialog = false)
                // Resume play
                val pendingDrama = _monetizationState.value.pendingDramaToUnlock ?: _playerState.value.currentDrama
                executePlayback(pendingDrama, episodeIndex)
            }
        }
    }

    fun startRewardedAdForEpisode(dramaId: String, episodeIndex: Int) {
        _monetizationState.value = _monetizationState.value.copy(
            showUnlockDialog = false,
            showRewardedAd = true,
            rewardedEpisodeIndexTarget = episodeIndex
        )
    }

    fun startRewardedAdForCoins() {
        val bounty = adminConfig.value.rewardedAdCoins
        _monetizationState.value = _monetizationState.value.copy(
            showRewardedAd = true,
            rewardedCoinsTarget = bounty,
            rewardedEpisodeIndexTarget = null
        )
    }

    fun onRewardEarned(coins: Int, episodeUnlocked: Int?) {
        viewModelScope.launch {
            if (episodeUnlocked != null) {
                val dramaId = _monetizationState.value.pendingDramaToUnlock?.id ?: _playerState.value.currentDrama.id
                repository.unlockEpisode(dramaId, episodeUnlocked)
                _monetizationState.value = _monetizationState.value.copy(
                    showRewardedAd = false,
                    rewardedEpisodeIndexTarget = null
                )
                val targetDrama = _monetizationState.value.pendingDramaToUnlock ?: _playerState.value.currentDrama
                executePlayback(targetDrama, episodeUnlocked)
            } else {
                val updatedWallet = userWallet.value.copy(coins = userWallet.value.coins + coins)
                repository.updateWallet(updatedWallet)
                _monetizationState.value = _monetizationState.value.copy(
                    showRewardedAd = false
                )
            }
        }
    }

    fun dismissRewardedAd() {
        _monetizationState.value = _monetizationState.value.copy(
            showRewardedAd = false,
            rewardedEpisodeIndexTarget = null
        )
    }

    fun openVipDialog() {
        _monetizationState.value = _monetizationState.value.copy(showLifetimePremiumScreen = true)
    }

    fun closeVipDialog() {
        _monetizationState.value = _monetizationState.value.copy(showVipDialog = false)
    }

    fun openLifetimePremiumScreen() {
        _monetizationState.value = _monetizationState.value.copy(showLifetimePremiumScreen = true)
    }

    fun closeLifetimePremiumScreen() {
        _monetizationState.value = _monetizationState.value.copy(showLifetimePremiumScreen = false)
        billingManager.resetState()
    }

    fun launchLifetimePurchase(activity: Activity) {
        billingManager.launchPurchaseFlow(activity)
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }

    fun toggleVipStatus() {
        viewModelScope.launch {
            val newVip = !userWallet.value.isVip
            repository.updateWallet(
                userWallet.value.copy(
                    isVip = newVip,
                    isLifetimePremium = if (!newVip) false else userWallet.value.isLifetimePremium
                )
            )
            _monetizationState.value = _monetizationState.value.copy(showVipDialog = false)
        }
    }

    fun updateAdminConfig(config: AdminConfigEntity) {
        viewModelScope.launch {
            repository.updateAdminConfig(config)
        }
    }

    fun addCoins(amount: Int) {
        viewModelScope.launch {
            repository.updateWallet(userWallet.value.copy(coins = userWallet.value.coins + amount))
        }
    }

    fun resetCoins() {
        viewModelScope.launch {
            repository.updateWallet(userWallet.value.copy(coins = 0))
        }
    }

    fun resetAllUnlocks() {
        viewModelScope.launch {
            repository.resetAllUnlocks()
        }
    }

    fun triggerTestInterstitial() {
        _monetizationState.value = _monetizationState.value.copy(showInterstitialAd = true)
    }

    fun triggerTestRewarded() {
        _monetizationState.value = _monetizationState.value.copy(
            showRewardedAd = true,
            rewardedCoinsTarget = adminConfig.value.rewardedAdCoins,
            rewardedEpisodeIndexTarget = null
        )
    }

    fun playUrl(url: String, customTitle: String = "Drama Streaming") {
        val normalizedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }

        val dynamicDrama = DramaItem(
            id = "custom-" + normalizedUrl.hashCode(),
            title = customTitle.ifBlank { "ReelShort Custom Stream" },
            url = normalizedUrl,
            description = "Streaming from $normalizedUrl",
            category = "Custom Stream",
            episodesCount = 1,
            rating = "5.0 ★",
            badge = "Direct",
            coverUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=600&auto=format&fit=crop&q=80"
        )

        requestPlayEpisode(dynamicDrama, 1)
    }

    fun onProgressUpdate(progress: Int) {
        _playerState.value = _playerState.value.copy(
            loadingProgress = progress,
            isLoading = progress in 1..99
        )
    }

    fun onPageTitleReceived(title: String) {
        if (title.isNotBlank() && !title.contains("Just a moment") && !title.contains("Cloudflare")) {
            _playerState.value = _playerState.value.copy(pageTitle = title)
            recordWatchHistory(
                _playerState.value.currentDrama.id,
                title,
                _playerState.value.activeUrl,
                _playerState.value.currentEpisodeName,
                _playerState.value.currentDrama.coverUrl
            )
        }
    }

    fun incrementBlockedAds() {
        _playerState.value = _playerState.value.copy(
            blockedAdsCount = _playerState.value.blockedAdsCount + 1
        )
    }

    fun toggleTheaterMaximized() {
        _playerState.value = _playerState.value.copy(
            isTheaterMaximized = !_playerState.value.isTheaterMaximized
        )
    }

    fun toggleBookmarkCurrent() {
        val current = _playerState.value.currentDrama
        val isBookmarked = isCurrentBookmarked.value
        viewModelScope.launch {
            repository.toggleBookmark(
                dramaId = current.id,
                title = _playerState.value.pageTitle.ifBlank { current.title },
                url = _playerState.value.activeUrl,
                description = current.description,
                coverUrl = current.coverUrl,
                category = current.category,
                currentlyBookmarked = isBookmarked
            )
        }
    }

    fun removeBookmark(dramaId: String) {
        viewModelScope.launch {
            repository.toggleBookmark(
                dramaId = dramaId,
                title = "",
                url = "",
                currentlyBookmarked = true
            )
        }
    }

    fun removeHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.removeHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    private fun recordWatchHistory(
        dramaId: String,
        title: String,
        url: String,
        episodeName: String,
        coverUrl: String
    ) {
        viewModelScope.launch {
            repository.recordWatch(
                dramaId = dramaId,
                title = title,
                url = url,
                episodeName = episodeName,
                coverUrl = coverUrl
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}
