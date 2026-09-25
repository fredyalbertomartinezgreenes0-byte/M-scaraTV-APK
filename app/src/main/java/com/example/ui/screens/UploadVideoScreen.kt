package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.MascaraDarkBg
import com.example.ui.theme.MascaraDramaSilver
import com.example.ui.theme.MascaraGold
import com.example.ui.theme.MascaraGoldLight
import com.example.ui.theme.MascaraGreen
import com.example.ui.theme.MascaraRed
import com.example.ui.theme.MascaraSurface
import com.example.ui.theme.MascaraSurfaceElevated
import com.example.ui.theme.MascaraSurfaceVariant
import com.example.ui.theme.MascaraTextMuted
import com.example.ui.viewmodel.UploadUiState

val UPLOAD_CATEGORIES = listOf("Comedia", "Drama", "Cine", "Música", "Gaming")
val VISIBILITY_OPTIONS = listOf("Público", "No listado", "Privado")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadVideoScreen(
    uploadState: UploadUiState,
    onBackClick: () -> Unit,
    onPublish: (
        title: String,
        description: String,
        category: String,
        tags: List<String>,
        videoUri: String,
        thumbnailUri: String,
        isShort: Boolean,
        visibility: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Comedia") }
    var tagsInput by remember { mutableStateOf("teatro, comedia, mascara") }
    var isShort by remember { mutableStateOf(false) }
    var visibilityIndex by remember { mutableIntStateOf(0) } // 0=Public, 1=Unlisted, 2=Private
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedThumbUri by remember { mutableStateOf<Uri?>(null) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    // Android Zero-permission Photo/Video Pickers (Google Play compliant)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedThumbUri = uri
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MascaraDarkBg)
            .testTag("upload_video_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isShort) "Subir Short a MáscaraTV" else "Subir Video a MáscaraTV",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Short vs Normal Video Selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MascaraSurfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "¿Es un video vertical (Short)?",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isShort) "Formato 9:16 para la sección de Shorts" else "Formato 16:9 tradicional para la pantalla principal",
                            color = MascaraDramaSilver,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isShort,
                        onCheckedChange = { isShort = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MascaraGold,
                            checkedTrackColor = MascaraSurfaceElevated
                        )
                    )
                }
            }

            // Video File Selector Box
            item {
                Surface(
                    color = MascaraSurfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MascaraSurfaceElevated),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        }
                        .testTag("pick_video_file_button")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MascaraGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedVideoUri != null) Icons.Default.Check else Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = if (selectedVideoUri != null) MascaraGreen else MascaraGold,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (selectedVideoUri != null) "¡Archivo de video seleccionado!" else "Seleccionar archivo de video",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Text(
                            text = if (selectedVideoUri != null)
                                selectedVideoUri.toString().takeLast(35)
                            else
                                "Toca para abrir la galería o almacenamiento (MP4, MKV, WebM)",
                            color = MascaraDramaSilver,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Thumbnail Selector Box
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MascaraSurfaceVariant)
                        .clickable {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedThumbUri != null) {
                        AsyncImage(
                            model = selectedThumbUri,
                            contentDescription = "Miniatura personalizada",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MascaraSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = MascaraGold, modifier = Modifier.size(28.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedThumbUri != null) "Miniatura personalizada elegida" else "Elegir miniatura del video",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Imagen sugerida en alta definición (16:9 o 9:16)",
                            color = MascaraTextMuted,
                            fontSize = 11.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Text("Explorar", fontSize = 11.sp, color = MascaraGoldLight)
                    }
                }
            }

            // Title Input
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del video (obligatorio)") },
                    placeholder = { Text("Ej: Monólogo cómico en la TV de medianoche...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_title_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MascaraGold,
                        unfocusedBorderColor = MascaraSurfaceElevated,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = MascaraGold,
                        unfocusedLabelColor = MascaraDramaSilver,
                        focusedContainerColor = MascaraSurfaceVariant,
                        unfocusedContainerColor = MascaraSurfaceVariant
                    ),
                    maxLines = 2
                )
            }

            // Description Input
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("Cuéntale a tu audiencia de qué trata esta obra teatral o escena cinematográfica...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_description_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MascaraGold,
                        unfocusedBorderColor = MascaraSurfaceElevated,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = MascaraGold,
                        unfocusedLabelColor = MascaraDramaSilver,
                        focusedContainerColor = MascaraSurfaceVariant,
                        unfocusedContainerColor = MascaraSurfaceVariant
                    ),
                    minLines = 3,
                    maxLines = 5
                )
            }

            // Category Dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría de MáscaraTV") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MascaraGold,
                            unfocusedBorderColor = MascaraSurfaceElevated,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = MascaraGold,
                            focusedContainerColor = MascaraSurfaceVariant,
                            unfocusedContainerColor = MascaraSurfaceVariant
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                        modifier = Modifier.background(MascaraSurfaceElevated)
                    ) {
                        UPLOAD_CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = Color.White) },
                                onClick = {
                                    category = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Tags Input
            item {
                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Etiquetas (separadas por comas)") },
                    placeholder = { Text("teatro, drama, shakespeare, humor") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MascaraGold,
                        unfocusedBorderColor = MascaraSurfaceElevated,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = MascaraGold,
                        unfocusedContainerColor = MascaraSurfaceVariant,
                        focusedContainerColor = MascaraSurfaceVariant
                    )
                )
            }

            // Visibility Segmented Buttons
            item {
                Column {
                    Text(
                        text = "Visibilidad",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        VISIBILITY_OPTIONS.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = visibilityIndex == index,
                                onClick = { visibilityIndex = index },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = VISIBILITY_OPTIONS.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MascaraGold,
                                    activeContentColor = MascaraDarkBg,
                                    inactiveContainerColor = MascaraSurfaceVariant,
                                    inactiveContentColor = Color.White
                                )
                            ) {
                                Text(option, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Upload Progress Area
            if (uploadState.isUploading) {
                item {
                    Surface(
                        color = MascaraSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uploadState.statusMessage,
                                    color = MascaraGoldLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${(uploadState.uploadProgress * 100).toInt()}%",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { uploadState.uploadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MascaraGold,
                                trackColor = MascaraSurfaceElevated
                            )
                        }
                    }
                }
            }

            // Submit Buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val finalTitle = title.ifBlank { "Mi Obra en Escena" }
                            val finalDesc = description.ifBlank { "Video creado y compartido por la comunidad de MáscaraTV." }
                            val tagsList = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val vidUriStr = selectedVideoUri?.toString() ?: ""
                            val thumbUriStr = selectedThumbUri?.toString() ?: ""

                            onPublish(
                                finalTitle,
                                finalDesc,
                                category,
                                tagsList,
                                vidUriStr,
                                thumbUriStr,
                                isShort,
                                VISIBILITY_OPTIONS[visibilityIndex]
                            )
                        },
                        enabled = !uploadState.isUploading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MascaraGold,
                            contentColor = MascaraDarkBg
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("publish_video_button"),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        if (uploadState.isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MascaraDarkBg, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Subiendo video...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publicar en MáscaraTV", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            // Save as draft
                            onBackClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Guardar como borrador", color = MascaraDramaSilver)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}
