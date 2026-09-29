package com.example.data

data class DramaItem(
    val id: String,
    val title: String,
    val url: String,
    val description: String,
    val category: String,
    val episodesCount: Int,
    val rating: String,
    val badge: String,
    val coverUrl: String,
    val isFeatured: Boolean = false
)

object DramaCatalog {
    val TARGET_FEATURED_DRAMA = DramaItem(
        id = "6a7d34b4528ba9b67e0fe6ce",
        title = "The Alpha King's AI Bride",
        url = "https://reelshort.dramafren.org/index.php?page=detail&id=6a7d34b4528ba9b67e0fe6ce",
        description = "In a neo-cyber realm where destiny clashes with high-tech algorithms, an undercover AI creator is chosen as the fated mate of the city's most ruthless billionaire Alpha King.",
        category = "AI Sci-Fi & Werewolf",
        episodesCount = 82,
        rating = "4.9 ★",
        badge = "Requested AI Drama",
        coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
        isFeatured = true
    )

    val TRENDING_DRAMAS = listOf(
        TARGET_FEATURED_DRAMA,
        DramaItem(
            id = "ai-cyber-heart-01",
            title = "Silicon Heart: The Android CEO",
            url = "https://reelshort.dramafren.org/index.php?page=detail&id=6a7d34b4528ba9b67e0fe6ce",
            description = "A bio-synthetic android disguised as a cold corporate titan unexpectedly develops forbidden human feelings for his brilliant assistant.",
            category = "AI Romance",
            episodesCount = 65,
            rating = "4.8 ★",
            badge = "Trending AI",
            coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80"
        ),
        DramaItem(
            id = "billionaire-secret-love",
            title = "Never Divorce a Secret Billionaire",
            url = "https://reelshort.dramafren.org/index.php?page=search&q=billionaire",
            description = "Treated as a nobody for three years, he reveals his trillion-dollar empire the day she demands a divorce.",
            category = "Billionaire & CEO",
            episodesCount = 95,
            rating = "4.9 ★",
            badge = "Top 1 Drama",
            coverUrl = "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=600&auto=format&fit=crop&q=80"
        ),
        DramaItem(
            id = "lycan-forbidden-mate",
            title = "The Lycan's True Mate",
            url = "https://reelshort.dramafren.org/index.php?page=search&q=lycan",
            description = "Exiled from her pack, she thought her life was over until the terrifying Lycan King claimed her in the shadows.",
            category = "Fantasy & Werewolf",
            episodesCount = 74,
            rating = "4.7 ★",
            badge = "Viral Hit",
            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80"
        ),
        DramaItem(
            id = "abandoned-heiress-revenge",
            title = "Revenge of the Abandoned Heiress",
            url = "https://reelshort.dramafren.org/index.php?page=search&q=revenge",
            description = "She returns five years later with supreme influence and a trio of prodigy children to make her treacherous family pay.",
            category = "Revenge & Drama",
            episodesCount = 88,
            rating = "4.8 ★",
            badge = "Must Watch",
            coverUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600&auto=format&fit=crop&q=80"
        ),
        DramaItem(
            id = "reelshort-explore-all",
            title = "Browse All ReelShort Dramas",
            url = "https://reelshort.dramafren.org/",
            description = "Access the entire DramaFren ReelShort library, trending daily updates, and newly unlocked short films.",
            category = "All Catalog",
            episodesCount = 500,
            rating = "5.0 ★",
            badge = "Full Hub",
            coverUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=600&auto=format&fit=crop&q=80"
        )
    )
}
