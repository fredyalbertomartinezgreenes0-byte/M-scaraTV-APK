package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AdminReportItem
import com.example.data.model.ChannelItem
import com.example.data.model.CommentItem
import com.example.data.model.NotificationItem
import com.example.data.model.UserAccount
import com.example.data.model.VideoItem
import com.example.data.repository.MascaraRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppDestination {
    HOME,
    TRENDING,
    SHORTS,
    LIBRARY,
    PROFILE,
    SEARCH,
    CHANNEL_DETAIL,
    CREATOR_STUDIO,
    ADMIN_PANEL,
    UPLOAD
}

data class UploadUiState(
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val statusMessage: String = "",
    val isSuccess: Boolean = false,
    val uploadedVideoId: String? = null
)

class MascaraViewModel(
    private val repository: MascaraRepository
) : ViewModel() {

    // Current navigation state
    private val _currentDestination = MutableStateFlow(AppDestination.HOME)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    // Navigation backstack support
    private val backStack = mutableListOf<AppDestination>()

    // Selected Category Chip
    private val _selectedCategory = MutableStateFlow("Para ti")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Active playing video (null if in feed)
    private val _activeVideo = MutableStateFlow<VideoItem?>(null)
    val activeVideo: StateFlow<VideoItem?> = _activeVideo.asStateFlow()

    // Video comments flow for active video
    val activeVideoComments: StateFlow<List<CommentItem>> = _activeVideo
        .flatMapLatest { video ->
            if (video != null) repository.getCommentsForVideo(video.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Fullscreen video toggle
    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    // Search query & filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow("Todos") // Todos, Más recientes, Más vistos, Cortos, Largos
    val searchFilter: StateFlow<String> = _searchFilter.asStateFlow()

    // Selected channel for detail view
    private val _selectedChannel = MutableStateFlow<ChannelItem?>(null)
    val selectedChannel: StateFlow<ChannelItem?> = _selectedChannel.asStateFlow()

    val channelVideos: StateFlow<List<VideoItem>> = _selectedChannel
        .flatMapLatest { channel ->
            if (channel != null) repository.getVideosByChannel(channel.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Modals / Dialogs
    private val _showNotificationsDialog = MutableStateFlow(false)
    val showNotificationsDialog: StateFlow<Boolean> = _showNotificationsDialog.asStateFlow()

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _shareVideoItem = MutableStateFlow<VideoItem?>(null)
    val shareVideoItem: StateFlow<VideoItem?> = _shareVideoItem.asStateFlow()

    private val _showCommentsSheet = MutableStateFlow(false)
    val showCommentsSheet: StateFlow<Boolean> = _showCommentsSheet.asStateFlow()

    // Upload state
    private val _uploadState = MutableStateFlow(UploadUiState())
    val uploadState: StateFlow<UploadUiState> = _uploadState.asStateFlow()

    // Data from repository
    val allVideos = repository.allVideos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val standardVideos = repository.standardVideos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val shortsVideos = repository.shortsVideos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allChannels = repository.allChannels.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val watchHistory = repository.watchHistoryVideos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val savedVideos = repository.savedVideos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val currentUser = repository.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserAccount())
    val notifications = repository.notifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val adminReports = repository.adminReports.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Feed based on category
    val feedVideos: StateFlow<List<VideoItem>> = combine(
        standardVideos,
        _selectedCategory
    ) { list, cat ->
        when (cat) {
            "Para ti" -> list
            "Tendencias" -> list.sortedByDescending { it.viewsCount }
            "Nuevos videos" -> list.sortedByDescending { it.id }
            else -> list.filter { it.category.equals(cat, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search results flow
    val searchResults: StateFlow<List<VideoItem>> = combine(
        allVideos,
        _searchQuery,
        _searchFilter
    ) { videos, query, filter ->
        var result = if (query.isBlank()) {
            videos
        } else {
            val q = query.trim().lowercase()
            videos.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.channelName.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.tags.any { tag -> tag.lowercase().contains(q) }
            }
        }

        when (filter) {
            "Más recientes" -> result.sortedByDescending { it.id }
            "Más vistos" -> result.sortedByDescending { it.viewsCount }
            "Cortos" -> result.filter { it.isShort || it.durationSeconds < 300 }
            "Largos" -> result.filter { !it.isShort && it.durationSeconds >= 600 }
            else -> result
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(dest: AppDestination) {
        if (_currentDestination.value != dest) {
            backStack.add(_currentDestination.value)
            _currentDestination.value = dest
        }
    }

    fun navigateBack(): Boolean {
        if (_activeVideo.value != null) {
            _activeVideo.value = null
            _isFullscreen.value = false
            return true
        }
        if (backStack.isNotEmpty()) {
            _currentDestination.value = backStack.removeAt(backStack.size - 1)
            return true
        }
        return false
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun openVideo(video: VideoItem) {
        _activeVideo.value = video
        viewModelScope.launch {
            repository.recordWatch(video.id)
        }
    }

    fun closeVideo() {
        _activeVideo.value = null
        _isFullscreen.value = false
    }

    fun toggleFullscreen() {
        _isFullscreen.value = !_isFullscreen.value
    }

    fun openChannel(channel: ChannelItem) {
        _selectedChannel.value = channel
        navigateTo(AppDestination.CHANNEL_DETAIL)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchFilter(filter: String) {
        _searchFilter.value = filter
    }

    fun toggleLike(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleLike(video.id)
            if (_activeVideo.value?.id == video.id) {
                _activeVideo.value = _activeVideo.value?.copy(
                    isLiked = !video.isLiked,
                    likesCount = if (!video.isLiked) video.likesCount + 1 else maxOf(0L, video.likesCount - 1),
                    isDisliked = false
                )
            }
        }
    }

    fun toggleDislike(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleDislike(video.id)
            if (_activeVideo.value?.id == video.id) {
                _activeVideo.value = _activeVideo.value?.copy(
                    isDisliked = !video.isDisliked,
                    dislikesCount = if (!video.isDisliked) video.dislikesCount + 1 else maxOf(0L, video.dislikesCount - 1),
                    isLiked = false
                )
            }
        }
    }

    fun toggleSave(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleSave(video.id)
            if (_activeVideo.value?.id == video.id) {
                _activeVideo.value = _activeVideo.value?.copy(
                    isSaved = !video.isSaved
                )
            }
        }
    }

    fun toggleDownload(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleDownload(video.id)
            if (_activeVideo.value?.id == video.id) {
                _activeVideo.value = _activeVideo.value?.copy(
                    isDownloaded = !video.isDownloaded
                )
            }
        }
    }

    fun toggleSubscribe(channelId: String) {
        viewModelScope.launch {
            repository.toggleSubscription(channelId)
            val currCh = _selectedChannel.value
            if (currCh != null && currCh.id == channelId) {
                _selectedChannel.value = currCh.copy(
                    isSubscribed = !currCh.isSubscribed,
                    subscribersCount = currCh.subscribersCount + (if (!currCh.isSubscribed) 1 else -1)
                )
            }
        }
    }

    fun addComment(videoId: String, text: String) {
        if (text.isBlank()) return
        val user = currentUser.value
        viewModelScope.launch {
            repository.addComment(videoId, text, user.name, user.avatar)
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            repository.deleteComment(commentId)
        }
    }

    fun likeComment(commentId: String) {
        viewModelScope.launch {
            repository.toggleCommentLike(commentId)
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearWatchHistory()
        }
    }

    fun openShareModal(video: VideoItem) {
        _shareVideoItem.value = video
    }

    fun closeShareModal() {
        _shareVideoItem.value = null
    }

    fun openNotifications() {
        _showNotificationsDialog.value = true
        repository.markNotificationsAsRead()
    }

    fun closeNotifications() {
        _showNotificationsDialog.value = false
    }

    fun openAuthDialog() {
        _showAuthDialog.value = true
    }

    fun closeAuthDialog() {
        _showAuthDialog.value = false
    }

    fun setCommentsSheetVisible(visible: Boolean) {
        _showCommentsSheet.value = visible
    }

    fun publishVideo(
        title: String,
        description: String,
        category: String,
        tags: List<String>,
        videoUri: String,
        thumbnailUri: String,
        isShort: Boolean,
        visibility: String
    ) {
        viewModelScope.launch {
            _uploadState.value = UploadUiState(
                isUploading = true,
                uploadProgress = 0.1f,
                statusMessage = "Cargando archivo de video..."
            )
            delay(500)
            _uploadState.value = _uploadState.value.copy(
                uploadProgress = 0.45f,
                statusMessage = "Generando transcodificación y miniaturas HD..."
            )
            delay(700)
            _uploadState.value = _uploadState.value.copy(
                uploadProgress = 0.85f,
                statusMessage = "Comprobando licencias teatrales y cinematográficas..."
            )
            delay(500)

            val newId = repository.publishVideo(
                title = title,
                description = description,
                category = category,
                tags = tags,
                videoUri = videoUri,
                thumbnailUri = thumbnailUri,
                isShort = isShort,
                visibility = visibility
            )

            _uploadState.value = UploadUiState(
                isUploading = false,
                uploadProgress = 1.0f,
                statusMessage = "¡Video publicado con éxito en MáscaraTV!",
                isSuccess = true,
                uploadedVideoId = newId
            )
            delay(800)
            navigateTo(if (isShort) AppDestination.SHORTS else AppDestination.HOME)
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadUiState()
    }

    fun saveChannelInfo(name: String, handle: String, bio: String, avatar: String, banner: String) {
        viewModelScope.launch {
            repository.createOrUpdateChannel(name, handle, bio, avatar, banner)
        }
    }

    fun adminDeleteVideo(videoId: String) {
        viewModelScope.launch {
            repository.deleteVideo(videoId)
        }
    }

    fun adminDismissReport(reportId: String) {
        repository.dismissReport(reportId)
    }

    fun loginDemoUser(name: String, email: String, avatar: String) {
        repository.updateUserSession(name, email, avatar)
        _showAuthDialog.value = false
    }

    companion object {
        fun provideFactory(repository: MascaraRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MascaraViewModel(repository) as T
                }
            }
    }
}
