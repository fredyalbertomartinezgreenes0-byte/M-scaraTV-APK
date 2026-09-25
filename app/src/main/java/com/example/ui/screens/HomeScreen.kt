package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.VideoItem
import com.example.ui.components.CategoryChipsRow
import com.example.ui.components.RetroTvFrame
import com.example.ui.components.VideoCard
import com.example.ui.theme.MascaraDarkBg
import com.example.ui.theme.MascaraDramaSilver
import com.example.ui.theme.MascaraGold
import com.example.ui.theme.MascaraGoldLight
import com.example.ui.theme.MascaraRed
import com.example.ui.theme.MascaraSurfaceElevated
import com.example.ui.theme.MascaraSurfaceVariant
import com.example.ui.theme.MascaraTextMuted

@Composable
fun HomeScreen(
    videos: List<VideoItem>,
    shorts: List<VideoItem>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onShortClick: (VideoItem) -> Unit,
    onChannelClick: (String) -> Unit,
    onSaveToggle: (VideoItem) -> Unit,
    onDownloadToggle: (VideoItem) -> Unit,
    onShareClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val featuredVideo = videos.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MascaraDarkBg)
            .testTag("home_screen_feed"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Sticky/Top Category chips
        item {
            CategoryChipsRow(
                selectedCategory = selectedCategory,
                onSelectCategory = onSelectCategory
            )
        }

        // Hero Featured Video with Retro TV Bezel frame (shown in "Para ti")
        if (selectedCategory == "Para ti" && featuredVideo != null) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    RetroTvFrame(
                        channelLabel = "ESTRENO EXCLUSIVO • ${featuredVideo.category.uppercase()}"
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clickable { onVideoClick(featuredVideo) }
                        ) {
                            AsyncImage(
                                model = featuredVideo.thumbnailUrl,
                                contentDescription = featuredVideo.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Gradient vignette
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                            )

                            // Play Button in center
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(MascaraGold.copy(alpha = 0.9f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Reproducir",
                                    tint = MascaraDarkBg,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // Info Overlay at bottom
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                            ) {
                                Surface(
                                    color = MascaraGold,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "EN CARTELERA",
                                        color = MascaraDarkBg,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = featuredVideo.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${featuredVideo.channelName} • ${featuredVideo.durationFormatted}",
                                    color = MascaraGoldLight,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Horizontal Carousel for Shorts preview (if on "Para ti" or "Shorts")
        if (shorts.isNotEmpty() && (selectedCategory == "Para ti" || selectedCategory == "Shorts")) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚡", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Shorts de MáscaraTV",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MascaraRed,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "VERTICAL",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(shorts) { shortItem ->
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(230.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MascaraSurfaceVariant)
                                    .clickable { onShortClick(shortItem) }
                                    .testTag("short_preview_${shortItem.id}")
                            ) {
                                AsyncImage(
                                    model = shortItem.thumbnailUrl,
                                    contentDescription = shortItem.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.85f)
                                                )
                                            )
                                        )
                                )

                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = shortItem.title,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${shortItem.viewsCount / 1000}K vistas",
                                        color = MascaraGoldLight,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section header for main feed
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedCategory) {
                        "Para ti" -> "Recomendados para ti"
                        "Tendencias" -> "🔥 Videos en Tendencia"
                        "Nuevos videos" -> "🆕 Recién Salidos del Escenario"
                        else -> "🎭 Categoría: $selectedCategory"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Video list
        val displayVideos = if (selectedCategory == "Para ti" && featuredVideo != null) {
            videos.filter { it.id != featuredVideo.id }
        } else {
            videos
        }

        if (displayVideos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron videos en esta categoría.",
                        color = MascaraTextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(displayVideos, key = { it.id }) { video ->
                VideoCard(
                    video = video,
                    onClick = { onVideoClick(video) },
                    onChannelClick = { onChannelClick(video.channelId) },
                    onSaveToggle = { onSaveToggle(video) },
                    onDownloadToggle = { onDownloadToggle(video) },
                    onShareClick = { onShareClick(video) },
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }
        }
    }
}
