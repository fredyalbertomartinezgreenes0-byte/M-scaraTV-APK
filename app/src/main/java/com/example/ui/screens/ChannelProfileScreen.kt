package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChannelItem
import com.example.data.model.UserAccount
import com.example.data.model.VideoItem
import com.example.ui.components.VideoCard
import com.example.ui.components.formatViews
import com.example.ui.theme.MascaraDarkBg
import com.example.ui.theme.MascaraDramaSilver
import com.example.ui.theme.MascaraGold
import com.example.ui.theme.MascaraGoldLight
import com.example.ui.theme.MascaraSurfaceElevated
import com.example.ui.theme.MascaraSurfaceVariant
import com.example.ui.theme.MascaraTextMuted

val CHANNEL_TABS = listOf("Videos", "Shorts", "Listas de reproducción", "Acerca de")

@Composable
fun ChannelProfileScreen(
    channel: ChannelItem?,
    currentUser: UserAccount,
    channelVideos: List<VideoItem>,
    onBackClick: () -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onSubscribeToggle: (String) -> Unit,
    onSaveChannelInfo: (String, String, String, String, String) -> Unit,
    onSaveToggle: (VideoItem) -> Unit,
    onDownloadToggle: (VideoItem) -> Unit,
    onShareVideo: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (channel == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MascaraDarkBg),
            contentAlignment = Alignment.Center
        ) {
            Text("Canal no encontrado", color = Color.White)
        }
        return
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MascaraDarkBg)
            .testTag("channel_profile_screen")
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text(
                text = channel.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            IconButton(onClick = { /* Share channel */ }) {
                Icon(Icons.Default.Share, contentDescription = "Compartir canal", tint = Color.White)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Channel Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    AsyncImage(
                        model = channel.bannerUrl,
                        contentDescription = "Banner del canal",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, MascaraDarkBg.copy(alpha = 0.8f))
                                )
                            )
                    )
                }
            }

            // Channel Header: Avatar, Name, Handle, Sub button, Description
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = channel.avatarUrl,
                            contentDescription = channel.name,
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .border(2.dp, MascaraGold, CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = channel.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                if (channel.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Verificado",
                                        tint = MascaraGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Text(
                                text = channel.handle,
                                color = MascaraDramaSilver,
                                fontSize = 13.sp
                            )

                            Text(
                                text = "${formatViews(channel.subscribersCount)} suscriptores • ${channelVideos.size} videos",
                                color = MascaraTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = channel.description,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Channel Action Button (Subscribe or Edit if owned)
                    if (channel.isUserOwned) {
                        OutlinedButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MascaraGold)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MascaraGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Editar canal", color = MascaraGold, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { onSubscribeToggle(channel.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (channel.isSubscribed) MascaraSurfaceElevated else MascaraGold,
                                contentColor = if (channel.isSubscribed) Color.White else MascaraDarkBg
                            )
                        ) {
                            if (channel.isSubscribed) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Suscrito", fontWeight = FontWeight.Bold)
                            } else {
                                Text("Suscribirse", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Tabs Row
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MascaraDarkBg,
                    contentColor = MascaraGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MascaraGold
                        )
                    },
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    CHANNEL_TABS.forEachIndexed { index, tabName ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = tabName,
                                    color = if (selectedTabIndex == index) MascaraGold else MascaraDramaSilver,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }

            // Content based on tab
            when (selectedTabIndex) {
                0 -> {
                    // Videos tab
                    val standardList = channelVideos.filter { !it.isShort }
                    if (standardList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay videos publicados en este canal.", color = MascaraTextMuted)
                            }
                        }
                    } else {
                        items(standardList, key = { it.id }) { video ->
                            VideoCard(
                                video = video,
                                onClick = { onVideoClick(video) },
                                onChannelClick = {},
                                onSaveToggle = { onSaveToggle(video) },
                                onDownloadToggle = { onDownloadToggle(video) },
                                onShareClick = { onShareVideo(video) },
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                1 -> {
                    // Shorts tab
                    val shortsList = channelVideos.filter { it.isShort }
                    if (shortsList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay shorts subidos en este canal.", color = MascaraTextMuted)
                            }
                        }
                    } else {
                        items(shortsList, key = { it.id }) { shortItem ->
                            VideoCard(
                                video = shortItem,
                                onClick = { onVideoClick(shortItem) },
                                onChannelClick = {},
                                onSaveToggle = { onSaveToggle(shortItem) },
                                onDownloadToggle = { onDownloadToggle(shortItem) },
                                onShareClick = { onShareVideo(shortItem) },
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                2 -> {
                    // Playlists tab
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = MascaraSurfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MascaraGold.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🎭", fontSize = 24.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Obras Teatrales Destacadas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Lista oficial de reproducciones • 8 videos", color = MascaraDramaSilver, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // About tab
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Descripción del canal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(channel.description, color = MascaraDramaSilver, fontSize = 13.sp, lineHeight = 18.sp)
                            Text("Más información", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("📍 Plataforma: MáscaraTV", color = MascaraDramaSilver, fontSize = 13.sp)
                            Text("🎭 Género principal: Teatro, Comedia & Cine", color = MascaraDramaSilver, fontSize = 13.sp)
                            Text("🔗 Enlace: mascaratv.com/${channel.handle}", color = MascaraGoldLight, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // Edit Channel Dialog
    if (showEditDialog) {
        var editName by remember { mutableStateOf(channel.name) }
        var editHandle by remember { mutableStateOf(channel.handle) }
        var editDesc by remember { mutableStateOf(channel.description) }
        var editAvatar by remember { mutableStateOf(channel.avatarUrl) }
        var editBanner by remember { mutableStateOf(channel.bannerUrl) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = MascaraSurfaceVariant,
            title = { Text("Personalizar mi Canal", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nombre del canal") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MascaraGold
                        )
                    )
                    OutlinedTextField(
                        value = editHandle,
                        onValueChange = { editHandle = it },
                        label = { Text("Handle (@usuario)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MascaraGold
                        )
                    )
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Descripción") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MascaraGold
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveChannelInfo(editName, editHandle, editDesc, editAvatar, editBanner)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MascaraGold, contentColor = MascaraDarkBg)
                ) {
                    Text("Guardar cambios", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar", color = Color.White)
                }
            }
        )
    }
}
