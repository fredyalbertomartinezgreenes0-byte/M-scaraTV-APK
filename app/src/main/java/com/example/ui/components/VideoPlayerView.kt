package com.example.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionDisabled
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.model.VideoItem
import com.example.ui.theme.MascaraDarkBg
import com.example.ui.theme.MascaraDramaSilver
import com.example.ui.theme.MascaraGold
import com.example.ui.theme.MascaraGoldLight
import com.example.ui.theme.MascaraRed
import com.example.ui.theme.MascaraSurfaceElevated
import com.example.ui.theme.MascaraSurfaceVariant
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerView(
    video: VideoItem,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var currentPositionSeconds by remember { mutableIntStateOf(0) }
    var durationSeconds by remember(video.id) { mutableIntStateOf(video.durationSeconds) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var selectedQuality by remember { mutableStateOf("1080p") }
    var subtitlesEnabled by remember { mutableStateOf(false) }
    var autoplayEnabled by remember { mutableStateOf(true) }
    var retroScanlinesEnabled by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    // Auto-advance progress when playing
    LaunchedEffect(isPlaying, durationSeconds) {
        while (isPlaying) {
            delay(1000)
            if (currentPositionSeconds < durationSeconds) {
                currentPositionSeconds += (1 * playbackSpeed).toInt().coerceAtLeast(1)
            } else {
                if (autoplayEnabled) {
                    currentPositionSeconds = 0
                } else {
                    isPlaying = false
                }
            }
        }
    }

    // Auto-hide controls after 4 seconds of idle
    LaunchedEffect(isControlsVisible, isPlaying) {
        if (isControlsVisible && isPlaying) {
            delay(4000)
            isControlsVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isFullscreen) Modifier.fillMaxSize() else Modifier.aspectRatio(16f / 9f))
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isControlsVisible = !isControlsVisible
            }
            .testTag("video_player_container")
    ) {
        // Underneath: Native VideoView if URL is valid, or fallback poster image
        var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

        if (video.videoUrl.isNotBlank() && (video.videoUrl.startsWith("http") || video.videoUrl.startsWith("content"))) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(Uri.parse(video.videoUrl))
                        setOnPreparedListener { mp ->
                            mp.isLooping = autoplayEnabled
                            if (duration > 0) durationSeconds = duration / 1000
                            if (isMuted) mp.setVolume(0f, 0f) else mp.setVolume(1f, 1f)
                            start()
                        }
                        setOnCompletionListener {
                            if (autoplayEnabled) {
                                start()
                            } else {
                                isPlaying = false
                            }
                        }
                        videoViewRef = this
                    }
                },
                update = { view ->
                    if (isPlaying && !view.isPlaying) {
                        view.start()
                    } else if (!isPlaying && view.isPlaying) {
                        view.pause()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Visual fallback with thumbnail
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Retro TV Scanline Canvas Filter (optional vintage effect)
        if (retroScanlinesEnabled) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 4.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.35f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.5f
                    )
                    y += step
                }
            }
        }

        // Subtitle Overlay text if enabled
        if (subtitlesEnabled && isPlaying) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (isControlsVisible) 64.dp else 24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when {
                        currentPositionSeconds % 10 < 4 -> "🎭 MáscaraTV: «La escena cobra vida en tu pantalla.»"
                        currentPositionSeconds % 10 < 7 -> "— ¡Silencio en el teatro! Comienza la función."
                        else -> "— Toda la verdad cabe en una máscara."
                    },
                    color = MascaraGoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // CONTROLS OVERLAY
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.75f),
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Top Bar Controls: Title + Settings
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = video.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Retro Scanlines toggle button
                        IconButton(
                            onClick = { retroScanlinesEnabled = !retroScanlinesEnabled },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = "Efecto TV Retro",
                                tint = if (retroScanlinesEnabled) MascaraGold else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Subtitles CC Button
                        IconButton(
                            onClick = { subtitlesEnabled = !subtitlesEnabled },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (subtitlesEnabled) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionDisabled,
                                contentDescription = "Subtítulos",
                                tint = if (subtitlesEnabled) MascaraGold else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Settings / Quality / Speed Menu
                        Box {
                            IconButton(
                                onClick = { showSettingsMenu = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Ajustes de reproducción",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showSettingsMenu,
                                onDismissRequest = { showSettingsMenu = false },
                                modifier = Modifier.background(MascaraSurfaceElevated)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Velocidad: ${playbackSpeed}x", color = Color.White) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Speed, contentDescription = null, tint = MascaraGold)
                                    },
                                    onClick = {
                                        playbackSpeed = when (playbackSpeed) {
                                            0.5f -> 1.0f
                                            1.0f -> 1.25f
                                            1.25f -> 1.5f
                                            1.5f -> 2.0f
                                            else -> 0.5f
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Calidad: $selectedQuality", color = Color.White) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Tv, contentDescription = null, tint = MascaraGold)
                                    },
                                    onClick = {
                                        selectedQuality = when (selectedQuality) {
                                            "1080p" -> "720p"
                                            "720p" -> "480p"
                                            "480p" -> "Auto"
                                            else -> "1080p"
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (autoplayEnabled) "Autoplay: Activado" else "Autoplay: Desactivado",
                                            color = Color.White
                                        )
                                    },
                                    onClick = { autoplayEnabled = !autoplayEnabled }
                                )
                            }
                        }
                    }
                }

                // Center Play/Pause & Seek Jump Buttons
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = {
                            currentPositionSeconds = (currentPositionSeconds - 10).coerceAtLeast(0)
                            videoViewRef?.seekTo(currentPositionSeconds * 1000)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Retroceder 10 segundos",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Main Play/Pause Button with Gold Halo
                    IconButton(
                        onClick = {
                            isPlaying = !isPlaying
                            if (isPlaying) videoViewRef?.start() else videoViewRef?.pause()
                        },
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MascaraGold)
                            .testTag("player_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = MascaraDarkBg,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Fast Forward 10s
                    IconButton(
                        onClick = {
                            currentPositionSeconds = (currentPositionSeconds + 10).coerceAtMost(durationSeconds)
                            videoViewRef?.seekTo(currentPositionSeconds * 1000)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Adelantar 10 segundos",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Bottom Timeline Controls: Progress Slider + Times + Volume + Fullscreen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    // Slider
                    Slider(
                        value = if (durationSeconds > 0) currentPositionSeconds.toFloat() / durationSeconds else 0f,
                        onValueChange = { progress ->
                            currentPositionSeconds = (progress * durationSeconds).toInt()
                            videoViewRef?.seekTo(currentPositionSeconds * 1000)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MascaraGold,
                            activeTrackColor = MascaraGold,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .testTag("player_seek_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Elapsed / Total Time
                        val currentFormatted = formatTimeSeconds(currentPositionSeconds)
                        val totalFormatted = formatTimeSeconds(durationSeconds)
                        Text(
                            text = "$currentFormatted / $totalFormatted",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Mute toggle
                            IconButton(
                                onClick = { isMuted = !isMuted },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                    contentDescription = if (isMuted) "Activar sonido" else "Silenciar",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Fullscreen Button
                            IconButton(
                                onClick = onToggleFullscreen,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("player_fullscreen_button")
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = if (isFullscreen) "Salir de pantalla completa" else "Pantalla completa",
                                    tint = MascaraGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTimeSeconds(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
