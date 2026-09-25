package com.example.data.repository

import com.example.data.local.ChannelDao
import com.example.data.local.ChannelEntity
import com.example.data.local.CommentDao
import com.example.data.local.CommentEntity
import com.example.data.local.HistoryAndSavedDao
import com.example.data.local.SavedVideoEntity
import com.example.data.local.VideoDao
import com.example.data.local.VideoEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.AdminReportItem
import com.example.data.model.ChannelItem
import com.example.data.model.CommentItem
import com.example.data.model.NotificationItem
import com.example.data.model.UserAccount
import com.example.data.model.VideoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class MascaraRepository(
    private val videoDao: VideoDao,
    private val channelDao: ChannelDao,
    private val commentDao: CommentDao,
    private val historyAndSavedDao: HistoryAndSavedDao
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Current User Session
    private val _currentUser = MutableStateFlow(
        UserAccount(
            id = "user_me",
            name = "Fredy Martínez",
            email = "fredy@mascaratv.com",
            avatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=160",
            channelId = "ch_my_channel",
            isAdmin = true
        )
    )
    val currentUser = _currentUser.asStateFlow()

    // Notifications In-Memory & Dynamic
    private val _notifications = MutableStateFlow<List<NotificationItem>>(
        listOf(
            NotificationItem(
                id = "n1",
                title = "Nuevo estreno de Comedia",
                message = "Teatro La Tramoya ha subido: 'El Médico a Palos (Acto 1)'.",
                timeAgo = "Hace 15 min",
                isRead = false,
                videoId = "vid_comedia_1"
            ),
            NotificationItem(
                id = "n2",
                title = "Tu video está en Tendencias",
                message = "¡Tu short 'Monólogo de la Máscara' superó las 1,000 reproducciones!",
                timeAgo = "Hace 2 horas",
                isRead = false,
                videoId = "short_1"
            ),
            NotificationItem(
                id = "n3",
                title = "Nuevo suscriptor",
                message = "El canal 'Cine Clásico TV' se ha suscrito a tu canal.",
                timeAgo = "Hace 1 día",
                isRead = true
            )
        )
    )
    val notifications = _notifications.asStateFlow()

    // Admin Reports
    private val _adminReports = MutableStateFlow<List<AdminReportItem>>(
        listOf(
            AdminReportItem(
                id = "rep_1",
                type = "VIDEO",
                targetId = "vid_drama_2",
                targetTitle = "Macbeth: La Sombra del Castillo",
                reason = "Presunto audio no sincronizado",
                reporterName = "UsuarioCritico",
                timeAgo = "Hace 3 horas",
                status = "Pendiente"
            ),
            AdminReportItem(
                id = "rep_2",
                type = "COMENTARIO",
                targetId = "c_seed_1",
                targetTitle = "Excelente puesta en escena...",
                reason = "Spam promocional",
                reporterName = "Espectador_99",
                timeAgo = "Hace 5 horas",
                status = "Pendiente"
            )
        )
    )
    val adminReports = _adminReports.asStateFlow()

    init {
        repositoryScope.launch {
            seedDatabaseIfEmpty()
        }
    }

    // Public Videos Flow
    val allVideos: Flow<List<VideoItem>> = videoDao.getAllVideos().map { list ->
        list.map { it.toDomain() }
    }

    val standardVideos: Flow<List<VideoItem>> = videoDao.getStandardVideos().map { list ->
        list.map { it.toDomain() }
    }

    val shortsVideos: Flow<List<VideoItem>> = videoDao.getShortsVideos().map { list ->
        list.map { it.toDomain() }
    }

    val allChannels: Flow<List<ChannelItem>> = channelDao.getAllChannels().map { list ->
        list.map { it.toDomain() }
    }

    // Watch History
    val watchHistoryVideos: Flow<List<VideoItem>> = combine(
        historyAndSavedDao.getWatchHistory(),
        allVideos
    ) { history, videos ->
        history.mapNotNull { hist -> videos.find { it.id == hist.videoId } }
    }

    // Saved Videos (Watch Later)
    val savedVideos: Flow<List<VideoItem>> = combine(
        historyAndSavedDao.getSavedVideos(),
        allVideos
    ) { saved, videos ->
        saved.mapNotNull { s -> videos.find { it.id == s.videoId } }
    }

    fun getVideoById(id: String): Flow<VideoItem?> {
        return videoDao.getVideoById(id).map { it?.toDomain() }
    }

    fun getCommentsForVideo(videoId: String): Flow<List<CommentItem>> {
        return commentDao.getCommentsForVideo(videoId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getVideosByChannel(channelId: String): Flow<List<VideoItem>> {
        return videoDao.getVideosByChannel(channelId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getChannelById(channelId: String): Flow<ChannelItem?> {
        return channelDao.getChannelById(channelId).map { it?.toDomain() }
    }

    suspend fun toggleLike(videoId: String) {
        val entity = videoDao.getVideoById(videoId).first() ?: return
        val newIsLiked = !entity.isLiked
        val newLikes = if (newIsLiked) entity.likesCount + 1 else maxOf(0L, entity.likesCount - 1)
        videoDao.updateLike(videoId, newLikes, newIsLiked)
    }

    suspend fun toggleDislike(videoId: String) {
        val entity = videoDao.getVideoById(videoId).first() ?: return
        val newIsDisliked = !entity.isDisliked
        val newDislikes = if (newIsDisliked) entity.dislikesCount + 1 else maxOf(0L, entity.dislikesCount - 1)
        videoDao.updateDislike(videoId, newDislikes, newIsDisliked)
    }

    suspend fun toggleSave(videoId: String) {
        val entity = videoDao.getVideoById(videoId).first() ?: return
        val newSaved = !entity.isSaved
        videoDao.updateSaved(videoId, newSaved)
        if (newSaved) {
            historyAndSavedDao.saveVideo(SavedVideoEntity(videoId = videoId))
        } else {
            historyAndSavedDao.unsaveVideo(videoId)
        }
    }

    suspend fun toggleDownload(videoId: String) {
        val entity = videoDao.getVideoById(videoId).first() ?: return
        val newDownloaded = !entity.isDownloaded
        videoDao.updateDownloaded(videoId, newDownloaded)
    }

    suspend fun recordWatch(videoId: String) {
        videoDao.incrementViews(videoId)
        historyAndSavedDao.addToHistory(WatchHistoryEntity(videoId = videoId))
    }

    suspend fun clearWatchHistory() {
        historyAndSavedDao.clearHistory()
    }

    suspend fun toggleSubscription(channelId: String) {
        val channel = channelDao.getChannelById(channelId).first() ?: return
        val newSubscribed = !channel.isSubscribed
        val delta = if (newSubscribed) 1L else -1L
        channelDao.updateSubscription(channelId, newSubscribed, delta)
    }

    suspend fun addComment(videoId: String, text: String, userName: String, userAvatar: String) {
        val comment = CommentEntity(
            id = UUID.randomUUID().toString(),
            videoId = videoId,
            userName = userName,
            userAvatar = userAvatar,
            text = text,
            timeAgo = "Ahora",
            likesCount = 0,
            isLiked = false,
            isOwner = true
        )
        commentDao.insertComment(comment)
    }

    suspend fun deleteComment(commentId: String) {
        commentDao.deleteComment(commentId)
    }

    suspend fun toggleCommentLike(commentId: String) {
        commentDao.updateCommentLike(commentId, 1, true)
    }

    suspend fun publishVideo(
        title: String,
        description: String,
        category: String,
        tags: List<String>,
        videoUri: String,
        thumbnailUri: String,
        isShort: Boolean,
        visibility: String
    ): String {
        val id = UUID.randomUUID().toString()
        val user = _currentUser.value
        val channel = channelDao.getChannelById(user.channelId ?: "ch_my_channel").first()
        val channelName = channel?.name ?: user.name
        val channelAvatar = channel?.avatarUrl ?: user.avatar

        val entity = VideoEntity(
            id = id,
            title = title,
            description = description,
            videoUrl = videoUri.ifBlank { "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" },
            thumbnailUrl = thumbnailUri.ifBlank { "https://images.unsplash.com/photo-1518173946687-a4c8a383392e?w=800" },
            durationFormatted = if (isShort) "0:45" else "08:12",
            durationSeconds = if (isShort) 45 else 492,
            viewsCount = 1,
            publishedTimeAgo = "Hace un momento",
            channelId = user.channelId ?: "ch_my_channel",
            channelName = channelName,
            channelAvatar = channelAvatar,
            isVerified = false,
            category = category,
            tagsJson = tags.joinToString(","),
            likesCount = 0,
            dislikesCount = 0,
            isLiked = false,
            isDisliked = false,
            isSaved = false,
            isDownloaded = false,
            isShort = isShort,
            allowDownload = true,
            visibility = visibility,
            localFilePath = videoUri
        )
        videoDao.insertVideo(entity)

        // Add to notifications
        val newNotif = NotificationItem(
            id = UUID.randomUUID().toString(),
            title = "Video publicado con éxito",
            message = "Tu video '$title' ya está disponible en MáscaraTV.",
            timeAgo = "Ahora",
            isRead = false,
            videoId = id
        )
        _notifications.value = listOf(newNotif) + _notifications.value

        return id
    }

    suspend fun createOrUpdateChannel(name: String, handle: String, description: String, avatarUrl: String, bannerUrl: String) {
        val user = _currentUser.value
        val channelId = user.channelId ?: "ch_my_channel"
        val channel = ChannelEntity(
            id = channelId,
            name = name,
            handle = if (handle.startsWith("@")) handle else "@$handle",
            description = description,
            avatarUrl = avatarUrl.ifBlank { user.avatar },
            bannerUrl = bannerUrl.ifBlank { "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1200" },
            subscribersCount = 42,
            isSubscribed = false,
            isVerified = false,
            isUserOwned = true,
            videoCount = 2
        )
        channelDao.insertChannel(channel)
        _currentUser.value = user.copy(name = name, avatar = avatarUrl.ifBlank { user.avatar })
    }

    suspend fun deleteVideo(videoId: String) {
        videoDao.deleteVideo(videoId)
        _adminReports.value = _adminReports.value.filter { it.targetId != videoId }
    }

    fun dismissReport(reportId: String) {
        _adminReports.value = _adminReports.value.map {
            if (it.id == reportId) it.copy(status = "Resuelto") else it
        }
    }

    fun markNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun updateUserSession(name: String, email: String, avatar: String) {
        _currentUser.value = _currentUser.value.copy(
            name = name,
            email = email,
            avatar = avatar
        )
    }

    private suspend fun seedDatabaseIfEmpty() {
        val existing = videoDao.getAllVideos().first()
        if (existing.isNotEmpty()) return

        // Channels
        val channels = listOf(
            ChannelEntity(
                id = "ch_comedia",
                name = "Comedia & Bambalinas",
                handle = "@comediabambalinas",
                description = "El mejor teatro cómico, entremeses, comedia del arte y humor escénico de habla hispana.",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                bannerUrl = "https://images.unsplash.com/photo-1514306191717-452ec28c7814?w=1200",
                subscribersCount = 142000,
                isSubscribed = true,
                isVerified = true,
                isUserOwned = false,
                videoCount = 48
            ),
            ChannelEntity(
                id = "ch_drama",
                name = "Dramaturgia Viva",
                handle = "@dramaturgiaviva",
                description = "Tragedias griegas, drama shakesperiano y obras contemporáneas con los más grandes actores.",
                avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                bannerUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1200",
                subscribersCount = 285000,
                isSubscribed = true,
                isVerified = true,
                isUserOwned = false,
                videoCount = 64
            ),
            ChannelEntity(
                id = "ch_cine_retro",
                name = "Cine Retro TV",
                handle = "@cineretrotv",
                description = "Joyas del cine clásico, película de culto restaurada y cortometrajes independientes.",
                avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200",
                bannerUrl = "https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=1200",
                subscribersCount = 98000,
                isSubscribed = false,
                isVerified = true,
                isUserOwned = false,
                videoCount = 32
            ),
            ChannelEntity(
                id = "ch_my_channel",
                name = "Canal de Fredy",
                handle = "@fredymartinez",
                description = "Espacio creativo oficial en MáscaraTV: teatro, cine experimental y monólogos.",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                bannerUrl = "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=1200",
                subscribersCount = 1250,
                isSubscribed = false,
                isVerified = false,
                isUserOwned = true,
                videoCount = 3
            )
        )
        channelDao.insertChannels(channels)

        // Seed Videos with reliable sample public video streaming URLs
        val videos = listOf(
            VideoEntity(
                id = "vid_comedia_1",
                title = "El Médico a Palos: Gran Escena Cómica en la TV Retro",
                description = "Una versión clásica y festiva de la inmortal comedia teatral adaptada para la pantalla chica con estética retro. Risa garantizada para toda la familia con la legendaria máscara de la comedia.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?w=800",
                durationFormatted = "14:28",
                durationSeconds = 868,
                viewsCount = 54200,
                publishedTimeAgo = "Hace 2 días",
                channelId = "ch_comedia",
                channelName = "Comedia & Bambalinas",
                channelAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                isVerified = true,
                category = "Comedia",
                tagsJson = "comedia,teatro,clasico,humor,mascaratv",
                likesCount = 3420,
                dislikesCount = 32,
                isLiked = true,
                isDisliked = false,
                isSaved = true,
                isDownloaded = false,
                isShort = false,
                allowDownload = true,
                visibility = "Público"
            ),
            VideoEntity(
                id = "vid_drama_1",
                title = "Hamlet frente al Espejo: Tragedia en Cinco Actos",
                description = "La soledad del príncipe y el monólogo cumbre de la tragedia occidental. Una magistral interpretación grabada con iluminación de época y contrastes escénicos profundos.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800",
                durationFormatted = "18:45",
                durationSeconds = 1125,
                viewsCount = 89100,
                publishedTimeAgo = "Hace 4 días",
                channelId = "ch_drama",
                channelName = "Dramaturgia Viva",
                channelAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                isVerified = true,
                category = "Drama",
                tagsJson = "drama,tragedia,teatro,shakespeare,cine",
                likesCount = 7650,
                dislikesCount = 45,
                isLiked = false,
                isDisliked = false,
                isSaved = false,
                isDownloaded = true,
                isShort = false,
                allowDownload = true,
                visibility = "Público"
            ),
            VideoEntity(
                id = "vid_cine_1",
                title = "Lágrimas de Acero: Corto de Ciencia Ficción & Cine Noir",
                description = "Un futuro alternativo donde el drama y los recuerdos se mezclan en una atmósfera cinematográfica inolvidable. Ganador de múltiples festivales independientes.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=800",
                durationFormatted = "12:14",
                durationSeconds = 734,
                viewsCount = 120500,
                publishedTimeAgo = "Hace 1 semana",
                channelId = "ch_cine_retro",
                channelName = "Cine Retro TV",
                channelAvatar = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200",
                isVerified = true,
                category = "Cine",
                tagsJson = "cine,noir,scifi,independiente,mascaratv",
                likesCount = 11200,
                dislikesCount = 88,
                isLiked = false,
                isDisliked = false,
                isSaved = true,
                isDownloaded = false,
                isShort = false,
                allowDownload = true,
                visibility = "Público"
            ),
            VideoEntity(
                id = "vid_musica_1",
                title = "Obertura de las Máscaras: Orquesta Sinfónica en Vivo",
                description = "El acompañamiento musical para el cambio de antifaz: de la risa desbordante al llanto dramático. Concierto grabado en directo desde el Gran Teatro Central.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=800",
                durationFormatted = "07:35",
                durationSeconds = 455,
                viewsCount = 33900,
                publishedTimeAgo = "Hace 3 días",
                channelId = "ch_drama",
                channelName = "Dramaturgia Viva",
                channelAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                isVerified = true,
                category = "Música",
                tagsJson = "musica,sinfonica,teatro,orquesta",
                likesCount = 2890,
                dislikesCount = 14,
                isLiked = false,
                isDisliked = false,
                isSaved = false,
                isDownloaded = false,
                isShort = false,
                allowDownload = true,
                visibility = "Público"
            ),
            VideoEntity(
                id = "vid_gaming_1",
                title = "El Gran Escape Teatral: Gameplay Narrativo en Directo",
                description = "Exploramos un videojuego de misterio y puzles ambientado dentro de un teatro de época victoriana lleno de trampas y máscaras secretas.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800",
                durationFormatted = "22:50",
                durationSeconds = 1370,
                viewsCount = 67200,
                publishedTimeAgo = "Hace 5 días",
                channelId = "ch_comedia",
                channelName = "Comedia & Bambalinas",
                channelAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                isVerified = true,
                category = "Gaming",
                tagsJson = "gaming,directo,misterio,aventura",
                likesCount = 4900,
                dislikesCount = 60,
                isLiked = false,
                isDisliked = false,
                isSaved = false,
                isDownloaded = false,
                isShort = false,
                allowDownload = true,
                visibility = "Público"
            ),
            // SHORTS
            VideoEntity(
                id = "short_1",
                title = "Cuando cambias de Comedia a Drama en 3 segundos 😂🎭",
                description = "El duelo de gestos entre la risa y el llanto. #Shorts #Comedia #Teatro",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800",
                durationFormatted = "0:35",
                durationSeconds = 35,
                viewsCount = 145000,
                publishedTimeAgo = "Hace 1 día",
                channelId = "ch_comedia",
                channelName = "Comedia & Bambalinas",
                channelAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                isVerified = true,
                category = "Shorts",
                tagsJson = "shorts,comedia,humor,teatro",
                likesCount = 18900,
                dislikesCount = 120,
                isLiked = true,
                isDisliked = false,
                isSaved = false,
                isDownloaded = false,
                isShort = true,
                allowDownload = false,
                visibility = "Público"
            ),
            VideoEntity(
                id = "short_2",
                title = "La mejor línea trágica jamás escrita en el cine 🎬✨",
                description = "Un momento de silencio y pura emoción. #Shorts #Drama #Cine",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=800",
                durationFormatted = "0:42",
                durationSeconds = 42,
                viewsCount = 98200,
                publishedTimeAgo = "Hace 3 días",
                channelId = "ch_drama",
                channelName = "Dramaturgia Viva",
                channelAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                isVerified = true,
                category = "Shorts",
                tagsJson = "shorts,drama,cine,actuacion",
                likesCount = 12400,
                dislikesCount = 78,
                isLiked = false,
                isDisliked = false,
                isSaved = true,
                isDownloaded = false,
                isShort = true,
                allowDownload = false,
                visibility = "Público"
            ),
            VideoEntity(
                id = "short_3",
                title = "Cómo encender una TV retro de los años 70 con estilo 📺",
                description = "El encanto de las perillas analógicas y el zumbido del tubo. #Shorts #RetroTV",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1522869635100-9f4c5e86aa37?w=800",
                durationFormatted = "0:28",
                durationSeconds = 28,
                viewsCount = 189000,
                publishedTimeAgo = "Hace 6 días",
                channelId = "ch_cine_retro",
                channelName = "Cine Retro TV",
                channelAvatar = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200",
                isVerified = true,
                category = "Shorts",
                tagsJson = "shorts,retrotv,nostalgia,vintage",
                likesCount = 23100,
                dislikesCount = 110,
                isLiked = false,
                isDisliked = false,
                isSaved = false,
                isDownloaded = false,
                isShort = true,
                allowDownload = false,
                visibility = "Público"
            )
        )
        videoDao.insertVideos(videos)

        // Seed Comments
        val comments = listOf(
            CommentEntity(
                id = "c_seed_1",
                videoId = "vid_comedia_1",
                userName = "Lucía Mendoza",
                userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=120",
                text = "¡Qué deleite de puesta en escena! La sincronía cómica es perfecta, me hizo reír de principio a fin.",
                timeAgo = "Hace 1 día",
                likesCount = 42,
                isLiked = true,
                isOwner = false
            ),
            CommentEntity(
                id = "c_seed_2",
                videoId = "vid_comedia_1",
                userName = "Carlos Barrientos",
                userAvatar = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=120",
                text = "El diseño de MáscaraTV con la estética de televisión vintage le queda fabuloso a estos videos de teatro.",
                timeAgo = "Hace 18 horas",
                likesCount = 19,
                isLiked = false,
                isOwner = false
            ),
            CommentEntity(
                id = "c_seed_3",
                videoId = "vid_drama_1",
                userName = "Elena Navarro",
                userAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=120",
                text = "Esa mirada al final del monólogo transmite todo el dolor de la tragedia. Excepcional.",
                timeAgo = "Hace 2 días",
                likesCount = 88,
                isLiked = false,
                isOwner = false
            )
        )
        for (c in comments) {
            commentDao.insertComment(c)
        }

        // Seed History
        historyAndSavedDao.addToHistory(WatchHistoryEntity(videoId = "vid_comedia_1"))
        historyAndSavedDao.addToHistory(WatchHistoryEntity(videoId = "vid_cine_1"))
        historyAndSavedDao.saveVideo(SavedVideoEntity(videoId = "vid_comedia_1"))
        historyAndSavedDao.saveVideo(SavedVideoEntity(videoId = "vid_cine_1"))
    }
}

private fun VideoEntity.toDomain(): VideoItem {
    return VideoItem(
        id = id,
        title = title,
        description = description,
        videoUrl = videoUrl,
        thumbnailUrl = thumbnailUrl,
        durationFormatted = durationFormatted,
        durationSeconds = durationSeconds,
        viewsCount = viewsCount,
        publishedTimeAgo = publishedTimeAgo,
        channelId = channelId,
        channelName = channelName,
        channelAvatar = channelAvatar,
        isVerified = isVerified,
        category = category,
        tags = if (tagsJson.isBlank()) emptyList() else tagsJson.split(","),
        likesCount = likesCount,
        dislikesCount = dislikesCount,
        isLiked = isLiked,
        isDisliked = isDisliked,
        isSaved = isSaved,
        isDownloaded = isDownloaded,
        isShort = isShort,
        allowDownload = allowDownload,
        visibility = visibility,
        localFilePath = localFilePath
    )
}

private fun ChannelEntity.toDomain(): ChannelItem {
    return ChannelItem(
        id = id,
        name = name,
        handle = handle,
        description = description,
        avatarUrl = avatarUrl,
        bannerUrl = bannerUrl,
        subscribersCount = subscribersCount,
        isSubscribed = isSubscribed,
        isVerified = isVerified,
        isUserOwned = isUserOwned,
        videoCount = videoCount
    )
}

private fun CommentEntity.toDomain(): CommentItem {
    return CommentItem(
        id = id,
        videoId = videoId,
        userName = userName,
        userAvatar = userAvatar,
        text = text,
        timeAgo = timeAgo,
        likesCount = likesCount,
        isLiked = isLiked,
        isOwner = isOwner
    )
}
