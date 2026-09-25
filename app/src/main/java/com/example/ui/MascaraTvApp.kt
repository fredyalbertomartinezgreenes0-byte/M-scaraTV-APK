package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.VideoItem
import com.example.ui.components.AuthDialog
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.MascaraTopBar
import com.example.ui.components.NotificationsDialog
import com.example.ui.components.ShareModal
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.ChannelProfileScreen
import com.example.ui.screens.CreatorStudioScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.ShortsScreen
import com.example.ui.screens.VideoDetailScreen
import com.example.ui.screens.UploadVideoScreen
import com.example.ui.theme.MascaraDarkBg
import com.example.ui.theme.MascaraDramaSilver
import com.example.ui.theme.MascaraGold
import com.example.ui.theme.MascaraGoldLight
import com.example.ui.theme.MascaraRed
import com.example.ui.theme.MascaraSurface
import com.example.ui.theme.MascaraSurfaceElevated
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.MascaraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MascaraTvApp(
    viewModel: MascaraViewModel,
    modifier: Modifier = Modifier
) {
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val activeVideo by viewModel.activeVideo.collectAsStateWithLifecycle()
    val isFullscreen by viewModel.isFullscreen.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val feedVideos by viewModel.feedVideos.collectAsStateWithLifecycle()
    val shortsVideos by viewModel.shortsVideos.collectAsStateWithLifecycle()
    val allVideos by viewModel.allVideos.collectAsStateWithLifecycle()
    val allChannels by viewModel.allChannels.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val savedVideos by viewModel.savedVideos.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val adminReports by viewModel.adminReports.collectAsStateWithLifecycle()
    val activeComments by viewModel.activeVideoComments.collectAsStateWithLifecycle()
    val selectedChannel by viewModel.selectedChannel.collectAsStateWithLifecycle()
    val channelVideos by viewModel.channelVideos.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchFilter by viewModel.searchFilter.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val uploadState by viewModel.uploadState.collectAsStateWithLifecycle()

    val showNotifications by viewModel.showNotificationsDialog.collectAsStateWithLifecycle()
    val showAuthDialog by viewModel.showAuthDialog.collectAsStateWithLifecycle()
    val shareVideoItem by viewModel.shareVideoItem.collectAsStateWithLifecycle()
    val showCommentsSheet by viewModel.showCommentsSheet.collectAsStateWithLifecycle()

    val commentsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val shareSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Hardware back press handling
    BackHandler {
        if (!viewModel.navigateBack()) {
            // Close or exit
        }
    }

    val unreadCount = notifications.count { !it.isRead }
    val showTopBar = activeVideo == null && currentDestination != AppDestination.SHORTS && currentDestination != AppDestination.SEARCH && !isFullscreen

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MascaraDarkBg),
        topBar = {
            if (showTopBar) {
                MascaraTopBar(
                    currentUser = currentUser,
                    unreadNotificationsCount = unreadCount,
                    onLogoClick = { viewModel.navigateTo(AppDestination.HOME) },
                    onSearchClick = { viewModel.navigateTo(AppDestination.SEARCH) },
                    onNotificationsClick = { viewModel.openNotifications() },
                    onProfileClick = {
                        val myChannel = allChannels.find { it.isUserOwned } ?: allChannels.firstOrNull()
                        if (myChannel != null) viewModel.openChannel(myChannel)
                    },
                    onAdminClick = { viewModel.navigateTo(AppDestination.ADMIN_PANEL) }
                )
            }
        },
        bottomBar = {
            if (activeVideo == null && !isFullscreen) {
                NavigationBar(
                    containerColor = MascaraSurface,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("mascara_bottom_navigation")
                ) {
                    // 1. Inicio
                    NavigationBarItem(
                        selected = currentDestination == AppDestination.HOME,
                        onClick = { viewModel.navigateTo(AppDestination.HOME) },
                        icon = {
                            Icon(
                                if (currentDestination == AppDestination.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Inicio"
                            )
                        },
                        label = { Text("Inicio", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MascaraGold,
                            selectedTextColor = MascaraGold,
                            unselectedIconColor = MascaraDramaSilver,
                            unselectedTextColor = MascaraDramaSilver,
                            indicatorColor = MascaraSurfaceElevated
                        ),
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    // 2. Tendencias
                    NavigationBarItem(
                        selected = currentDestination == AppDestination.TRENDING,
                        onClick = {
                            viewModel.selectCategory("Tendencias")
                            viewModel.navigateTo(AppDestination.HOME)
                        },
                        icon = {
                            Icon(
                                if (selectedCategory == "Tendencias") Icons.Filled.LocalFireDepartment else Icons.Outlined.LocalFireDepartment,
                                contentDescription = "Tendencias"
                            )
                        },
                        label = { Text("Tendencias", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MascaraGold,
                            selectedTextColor = MascaraGold,
                            unselectedIconColor = MascaraDramaSilver,
                            unselectedTextColor = MascaraDramaSilver,
                            indicatorColor = MascaraSurfaceElevated
                        ),
                        modifier = Modifier.testTag("nav_item_trending")
                    )

                    // 3. Crear (Elevated Golden Button)
                    NavigationBarItem(
                        selected = currentDestination == AppDestination.UPLOAD,
                        onClick = { viewModel.navigateTo(AppDestination.UPLOAD) },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MascaraGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Crear",
                                    tint = MascaraDarkBg,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = { Text("Crear", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MascaraGold) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_item_upload")
                    )

                    // 4. Shorts
                    NavigationBarItem(
                        selected = currentDestination == AppDestination.SHORTS,
                        onClick = { viewModel.navigateTo(AppDestination.SHORTS) },
                        icon = {
                            Text(text = "⚡", fontSize = 20.sp)
                        },
                        label = { Text("Shorts", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MascaraGold,
                            selectedTextColor = MascaraGold,
                            unselectedIconColor = MascaraDramaSilver,
                            unselectedTextColor = MascaraDramaSilver,
                            indicatorColor = MascaraSurfaceElevated
                        ),
                        modifier = Modifier.testTag("nav_item_shorts")
                    )

                    // 5. Biblioteca
                    NavigationBarItem(
                        selected = currentDestination == AppDestination.LIBRARY,
                        onClick = { viewModel.navigateTo(AppDestination.LIBRARY) },
                        icon = {
                            Icon(
                                if (currentDestination == AppDestination.LIBRARY) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary,
                                contentDescription = "Biblioteca"
                            )
                        },
                        label = { Text("Biblioteca", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MascaraGold,
                            selectedTextColor = MascaraGold,
                            unselectedIconColor = MascaraDramaSilver,
                            unselectedTextColor = MascaraDramaSilver,
                            indicatorColor = MascaraSurfaceElevated
                        ),
                        modifier = Modifier.testTag("nav_item_library")
                    )
                }
            }
        },
        containerColor = MascaraDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main content depending on activeVideo or destination
            val currentVid = activeVideo
            if (currentVid != null) {
                // Active Playing Video Screen
                VideoDetailScreen(
                    video = currentVid,
                    comments = activeComments,
                    relatedVideos = allVideos,
                    isSubscribed = allChannels.find { it.id == currentVid.channelId }?.isSubscribed ?: false,
                    isFullscreen = isFullscreen,
                    onBackClick = { viewModel.closeVideo() },
                    onToggleFullscreen = { viewModel.toggleFullscreen() },
                    onLikeToggle = { viewModel.toggleLike(currentVid) },
                    onDislikeToggle = { viewModel.toggleDislike(currentVid) },
                    onSaveToggle = { viewModel.toggleSave(currentVid) },
                    onDownloadToggle = { viewModel.toggleDownload(currentVid) },
                    onShareClick = { viewModel.openShareModal(currentVid) },
                    onSubscribeToggle = { viewModel.toggleSubscribe(currentVid.channelId) },
                    onChannelClick = { chId ->
                        val ch = allChannels.find { it.id == chId }
                        if (ch != null) viewModel.openChannel(ch)
                    },
                    onOpenComments = { viewModel.setCommentsSheetVisible(true) },
                    onRelatedVideoClick = { relVideo -> viewModel.openVideo(relVideo) }
                )
            } else {
                // Screen Destinations
                when (currentDestination) {
                    AppDestination.HOME, AppDestination.TRENDING -> {
                        HomeScreen(
                            videos = feedVideos,
                            shorts = shortsVideos,
                            selectedCategory = selectedCategory,
                            onSelectCategory = { cat ->
                                if (cat == "Shorts") {
                                    viewModel.navigateTo(AppDestination.SHORTS)
                                } else if (cat == "Creadores") {
                                    val topChannel = allChannels.firstOrNull()
                                    if (topChannel != null) viewModel.openChannel(topChannel)
                                } else {
                                    viewModel.selectCategory(cat)
                                }
                            },
                            onVideoClick = { vid -> viewModel.openVideo(vid) },
                            onShortClick = { shortVid ->
                                viewModel.navigateTo(AppDestination.SHORTS)
                            },
                            onChannelClick = { chId ->
                                val ch = allChannels.find { it.id == chId }
                                if (ch != null) viewModel.openChannel(ch)
                            },
                            onSaveToggle = { vid -> viewModel.toggleSave(vid) },
                            onDownloadToggle = { vid -> viewModel.toggleDownload(vid) },
                            onShareClick = { vid -> viewModel.openShareModal(vid) }
                        )
                    }

                    AppDestination.SHORTS -> {
                        ShortsScreen(
                            shorts = shortsVideos,
                            onLikeToggle = { vid -> viewModel.toggleLike(vid) },
                            onOpenComments = { vid ->
                                viewModel.openVideo(vid)
                                viewModel.setCommentsSheetVisible(true)
                            },
                            onShareClick = { vid -> viewModel.openShareModal(vid) },
                            onSubscribeToggle = { chId -> viewModel.toggleSubscribe(chId) },
                            onChannelClick = { chId ->
                                val ch = allChannels.find { it.id == chId }
                                if (ch != null) viewModel.openChannel(ch)
                            }
                        )
                    }

                    AppDestination.SEARCH -> {
                        SearchScreen(
                            searchQuery = searchQuery,
                            searchFilter = searchFilter,
                            searchResults = searchResults,
                            channels = allChannels,
                            onQueryChange = { q -> viewModel.setSearchQuery(q) },
                            onFilterChange = { f -> viewModel.setSearchFilter(f) },
                            onBackClick = { viewModel.navigateBack() },
                            onVideoClick = { vid -> viewModel.openVideo(vid) },
                            onChannelClick = { chId ->
                                val ch = allChannels.find { it.id == chId }
                                if (ch != null) viewModel.openChannel(ch)
                            },
                            onSaveToggle = { vid -> viewModel.toggleSave(vid) },
                            onDownloadToggle = { vid -> viewModel.toggleDownload(vid) },
                            onShareClick = { vid -> viewModel.openShareModal(vid) }
                        )
                    }

                    AppDestination.UPLOAD -> {
                        UploadVideoScreen(
                            uploadState = uploadState,
                            onBackClick = { viewModel.navigateBack() },
                            onPublish = { title, desc, cat, tags, vidUri, thumbUri, isShort, vis ->
                                viewModel.publishVideo(title, desc, cat, tags, vidUri, thumbUri, isShort, vis)
                            }
                        )
                    }

                    AppDestination.LIBRARY, AppDestination.PROFILE -> {
                        LibraryScreen(
                            currentUser = currentUser,
                            historyVideos = watchHistory,
                            savedVideos = savedVideos,
                            allVideos = allVideos,
                            channels = allChannels,
                            onVideoClick = { vid -> viewModel.openVideo(vid) },
                            onChannelClick = { chId ->
                                val ch = allChannels.find { it.id == chId }
                                if (ch != null) viewModel.openChannel(ch)
                            },
                            onClearHistory = { viewModel.clearWatchHistory() },
                            onOpenCreatorStudio = { viewModel.navigateTo(AppDestination.CREATOR_STUDIO) },
                            onOpenAdminPanel = { viewModel.navigateTo(AppDestination.ADMIN_PANEL) },
                            onOpenAuthDialog = { viewModel.openAuthDialog() }
                        )
                    }

                    AppDestination.CHANNEL_DETAIL -> {
                        ChannelProfileScreen(
                            channel = selectedChannel,
                            currentUser = currentUser,
                            channelVideos = channelVideos,
                            onBackClick = { viewModel.navigateBack() },
                            onVideoClick = { vid -> viewModel.openVideo(vid) },
                            onSubscribeToggle = { chId -> viewModel.toggleSubscribe(chId) },
                            onSaveChannelInfo = { name, handle, desc, av, ban ->
                                viewModel.saveChannelInfo(name, handle, desc, av, ban)
                            },
                            onSaveToggle = { vid -> viewModel.toggleSave(vid) },
                            onDownloadToggle = { vid -> viewModel.toggleDownload(vid) },
                            onShareVideo = { vid -> viewModel.openShareModal(vid) }
                        )
                    }

                    AppDestination.CREATOR_STUDIO -> {
                        CreatorStudioScreen(
                            currentUser = currentUser,
                            channelVideos = channelVideos,
                            onBackClick = { viewModel.navigateBack() },
                            onUploadClick = { viewModel.navigateTo(AppDestination.UPLOAD) },
                            onVideoClick = { vid -> viewModel.openVideo(vid) },
                            onDeleteVideo = { vidId -> viewModel.adminDeleteVideo(vidId) }
                        )
                    }

                    AppDestination.ADMIN_PANEL -> {
                        AdminPanelScreen(
                            reports = adminReports,
                            videos = allVideos,
                            onBackClick = { viewModel.navigateBack() },
                            onDeleteVideo = { vidId -> viewModel.adminDeleteVideo(vidId) },
                            onDismissReport = { repId -> viewModel.adminDismissReport(repId) }
                        )
                    }
                }
            }
        }
    }

    // Comments Sheet
    if (showCommentsSheet && activeVideo != null) {
        CommentsBottomSheet(
            comments = activeComments,
            currentUser = currentUser,
            sheetState = commentsSheetState,
            onDismiss = { viewModel.setCommentsSheetVisible(false) },
            onAddComment = { text -> viewModel.addComment(activeVideo!!.id, text) },
            onLikeComment = { id -> viewModel.likeComment(id) },
            onDeleteComment = { id -> viewModel.deleteComment(id) }
        )
    }

    // Share Sheet
    if (shareVideoItem != null) {
        ShareModal(
            video = shareVideoItem!!,
            sheetState = shareSheetState,
            onDismiss = { viewModel.closeShareModal() }
        )
    }

    // Notifications Dialog
    if (showNotifications) {
        NotificationsDialog(
            notifications = notifications,
            onDismiss = { viewModel.closeNotifications() },
            onNotificationClick = { vidId ->
                viewModel.closeNotifications()
                if (vidId != null) {
                    val vid = allVideos.find { it.id == vidId }
                    if (vid != null) viewModel.openVideo(vid)
                }
            }
        )
    }

    // Auth / Switch Account Dialog
    if (showAuthDialog) {
        AuthDialog(
            currentUser = currentUser,
            onDismiss = { viewModel.closeAuthDialog() },
            onLoginDemo = { name, email, avatar ->
                viewModel.loginDemoUser(name, email, avatar)
            }
        )
    }
}
