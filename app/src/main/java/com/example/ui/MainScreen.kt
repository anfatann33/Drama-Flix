package com.example.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextTertiary

@Composable
fun MainScreen(
    viewModel: DramaViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val monetizationState by viewModel.monetizationState.collectAsStateWithLifecycle()
    val userWallet by viewModel.userWallet.collectAsStateWithLifecycle()
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()
    val currentUnlockedEpisodes by viewModel.currentUnlockedEpisodes.collectAsStateWithLifecycle()
    val isBookmarked by viewModel.isCurrentBookmarked.collectAsStateWithLifecycle()
    val recentHistory by viewModel.recentHistory.collectAsStateWithLifecycle()
    val fullHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val billingUiState by viewModel.billingUiState.collectAsStateWithLifecycle()

    // Handle back button when on sub-screens or modals
    BackHandler(enabled = monetizationState.showLifetimePremiumScreen || selectedTab != NavTab.DISCOVER) {
        if (monetizationState.showLifetimePremiumScreen) {
            viewModel.closeLifetimePremiumScreen()
        } else {
            viewModel.selectTab(NavTab.DISCOVER)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = DeepObsidian,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .testTag("main_bottom_nav_bar")
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == NavTab.DISCOVER,
                    onClick = { viewModel.selectTab(NavTab.DISCOVER) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == NavTab.DISCOVER) Icons.Filled.Explore else Icons.Outlined.Explore,
                            contentDescription = "Discover"
                        )
                    },
                    label = { Text("Discover", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonViolet,
                        selectedTextColor = TextPrimary,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary,
                        indicatorColor = NeonViolet.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_discover")
                )

                NavigationBarItem(
                    selected = selectedTab == NavTab.CINEMA,
                    onClick = { viewModel.selectTab(NavTab.CINEMA) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == NavTab.CINEMA) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircle,
                            contentDescription = "Cinema"
                        )
                    },
                    label = { Text("Cinema", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberGold,
                        selectedTextColor = TextPrimary,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary,
                        indicatorColor = AmberGold.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_cinema")
                )

                NavigationBarItem(
                    selected = selectedTab == NavTab.SAVED,
                    onClick = { viewModel.selectTab(NavTab.SAVED) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == NavTab.SAVED) Icons.Filled.Bookmarks else Icons.Outlined.Bookmarks,
                            contentDescription = "Saved"
                        )
                    },
                    label = { Text("Saved", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonViolet,
                        selectedTextColor = TextPrimary,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary,
                        indicatorColor = NeonViolet.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_saved")
                )

                NavigationBarItem(
                    selected = selectedTab == NavTab.HISTORY,
                    onClick = { viewModel.selectTab(NavTab.HISTORY) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == NavTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("History", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonViolet,
                        selectedTextColor = TextPrimary,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary,
                        indicatorColor = NeonViolet.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )

                NavigationBarItem(
                    selected = selectedTab == NavTab.ADMIN,
                    onClick = { viewModel.selectTab(NavTab.ADMIN) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == NavTab.ADMIN) Icons.Filled.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                            contentDescription = "Admin"
                        )
                    },
                    label = { Text("Admin", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberGold,
                        selectedTextColor = TextPrimary,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary,
                        indicatorColor = AmberGold.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_admin")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavTab.DISCOVER -> {
                    HomeScreen(
                        userWallet = userWallet,
                        freeEpisodesLimit = adminConfig.freeEpisodesPerDrama,
                        unlockedEpisodes = currentUnlockedEpisodes,
                        recentHistory = recentHistory,
                        onDramaClick = { drama -> viewModel.requestPlayEpisode(drama, 1) },
                        onEpisodeSelect = { drama, epIndex -> viewModel.requestPlayEpisode(drama, epIndex) },
                        onCustomUrlSubmit = { url -> viewModel.playUrl(url) },
                        onEarnCoinsClick = { viewModel.startRewardedAdForCoins() },
                        onVipBannerClick = { viewModel.openVipDialog() }
                    )
                }
                NavTab.CINEMA -> {
                    PlayerScreen(
                        playerState = playerState,
                        userWallet = userWallet,
                        isBookmarked = isBookmarked,
                        onBackClick = { viewModel.selectTab(NavTab.DISCOVER) },
                        onProgressUpdate = { viewModel.onProgressUpdate(it) },
                        onPageTitleReceived = { viewModel.onPageTitleReceived(it) },
                        onAdBlocked = { viewModel.incrementBlockedAds() },
                        onToggleBookmark = { viewModel.toggleBookmarkCurrent() },
                        onNextEpisodeClick = {
                            val nextEp = playerState.currentEpisodeIndex + 1
                            viewModel.requestPlayEpisode(playerState.currentDrama, nextEp)
                        },
                        onEarnCoinsClick = { viewModel.startRewardedAdForCoins() },
                        onVipClick = { viewModel.openVipDialog() }
                    )
                }
                NavTab.SAVED -> {
                    LibraryScreen(
                        bookmarks = bookmarks,
                        onDramaClick = { drama -> viewModel.requestPlayEpisode(drama, 1) },
                        onRemoveBookmark = { id -> viewModel.removeBookmark(id) },
                        onExploreClick = { viewModel.selectTab(NavTab.DISCOVER) }
                    )
                }
                NavTab.HISTORY -> {
                    HistoryScreen(
                        history = fullHistory,
                        onDramaClick = { drama -> viewModel.requestPlayEpisode(drama, 1) },
                        onRemoveHistoryItem = { id -> viewModel.removeHistoryItem(id) },
                        onClearAllHistory = { viewModel.clearAllHistory() },
                        onExploreClick = { viewModel.selectTab(NavTab.DISCOVER) }
                    )
                }
                NavTab.ADMIN -> {
                    AdminScreen(
                        adminConfig = adminConfig,
                        userWallet = userWallet,
                        onUpdateConfig = { viewModel.updateAdminConfig(it) },
                        onToggleVip = { viewModel.toggleVipStatus() },
                        onAddCoins = { viewModel.addCoins(it) },
                        onResetCoins = { viewModel.resetCoins() },
                        onResetUnlocks = { viewModel.resetAllUnlocks() },
                        onTriggerTestInterstitial = { viewModel.triggerTestInterstitial() },
                        onTriggerTestRewarded = { viewModel.triggerTestRewarded() }
                    )
                }
            }

            // Monetization Modal: Interstitial Ad
            if (monetizationState.showInterstitialAd) {
                InterstitialAdDialog(
                    adDurationSeconds = adminConfig.adDurationSeconds,
                    allowSkipAfterSeconds = adminConfig.allowSkipAfterSeconds,
                    onAdCompletedOrSkipped = { viewModel.onInterstitialAdFinished() }
                )
            }

            // Monetization Modal: Rewarded Ad
            if (monetizationState.showRewardedAd) {
                RewardedAdDialog(
                    rewardCoins = monetizationState.rewardedCoinsTarget,
                    targetEpisodeIndex = monetizationState.rewardedEpisodeIndexTarget,
                    onRewardEarned = { coins, epIndex -> viewModel.onRewardEarned(coins, epIndex) },
                    onDismiss = { viewModel.dismissRewardedAd() }
                )
            }

            // Monetization Modal: Unlock Episode Dialog
            if (monetizationState.showUnlockDialog) {
                val drama = monetizationState.pendingDramaToUnlock ?: playerState.currentDrama
                val epIndex = monetizationState.pendingEpisodeIndexToUnlock
                UnlockEpisodeDialog(
                    dramaTitle = drama.title,
                    episodeIndex = epIndex,
                    userCoins = userWallet.coins,
                    coinCost = adminConfig.episodeCoinCost,
                    onWatchAdToUnlock = { viewModel.startRewardedAdForEpisode(drama.id, epIndex) },
                    onPayCoinsToUnlock = { viewModel.unlockEpisodeWithCoins(drama.id, epIndex) },
                    onGoVipClick = {
                        viewModel.dismissUnlockDialog()
                        viewModel.openVipDialog()
                    },
                    onDismiss = { viewModel.dismissUnlockDialog() }
                )
            }

            // Monetization Modal: VIP Pass Dialog
            if (monetizationState.showVipDialog) {
                VipPassDialog(
                    isCurrentVip = userWallet.isVip,
                    onToggleVip = { viewModel.toggleVipStatus() },
                    onDismiss = { viewModel.closeVipDialog() }
                )
            }

            // Lifetime Premium Screen ($4.99 One-Time Non-Consumable Google Play Purchase)
            if (monetizationState.showLifetimePremiumScreen) {
                LifetimePremiumScreen(
                    userWallet = userWallet,
                    billingUiState = billingUiState,
                    onUnlockClicked = {
                        activity?.let { viewModel.launchLifetimePurchase(it) }
                    },
                    onRestoreClicked = { viewModel.restorePurchases() },
                    onCloseClicked = { viewModel.closeLifetimePremiumScreen() }
                )
            }
        }
    }
}
