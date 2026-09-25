package com.example.data.model

data class VideoItem(
    val id: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val durationFormatted: String,
    val durationSeconds: Int = 180,
    val viewsCount: Long,
    val publishedTimeAgo: String,
    val channelId: String,
    val channelName: String,
    val channelAvatar: String,
    val isVerified: Boolean = false,
    val category: String,
    val tags: List<String> = emptyList(),
    val likesCount: Long = 0,
    val dislikesCount: Long = 0,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val isSaved: Boolean = false,
    val isDownloaded: Boolean = false,
    val isShort: Boolean = false,
    val allowDownload: Boolean = true,
    val visibility: String = "Público",
    val localFilePath: String? = null
)

data class ChannelItem(
    val id: String,
    val name: String,
    val handle: String,
    val description: String,
    val avatarUrl: String,
    val bannerUrl: String,
    val subscribersCount: Long,
    val isSubscribed: Boolean = false,
    val isVerified: Boolean = false,
    val isUserOwned: Boolean = false,
    val videoCount: Int = 0
)

data class CommentItem(
    val id: String,
    val videoId: String,
    val userName: String,
    val userAvatar: String,
    val text: String,
    val timeAgo: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val isOwner: Boolean = false,
    val replies: List<CommentReply> = emptyList()
)

data class CommentReply(
    val id: String,
    val userName: String,
    val userAvatar: String,
    val text: String,
    val timeAgo: String,
    val likesCount: Int = 0
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val isRead: Boolean = false,
    val videoId: String? = null,
    val type: String = "VIDEO" // VIDEO, COMMENT, LIKE, SUBSCRIBER
)

data class UserAccount(
    val id: String = "user_current",
    val name: String = "Espectador Máscara",
    val email: String = "espectador@mascaratv.com",
    val avatar: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
    val channelId: String? = "ch_my_channel",
    val isAdmin: Boolean = true
)

data class AdminReportItem(
    val id: String,
    val type: String, // "VIDEO" or "COMENTARIO"
    val targetId: String,
    val targetTitle: String,
    val reason: String,
    val reporterName: String,
    val timeAgo: String,
    val status: String = "Pendiente" // Pendiente, Resuelto, Desestimado
)
