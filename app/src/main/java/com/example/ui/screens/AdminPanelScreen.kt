package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminReportItem
import com.example.data.model.VideoItem
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

@Composable
fun AdminPanelScreen(
    reports: List<AdminReportItem>,
    videos: List<VideoItem>,
    onBackClick: () -> Unit,
    onDeleteVideo: (String) -> Unit,
    onDismissReport: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MascaraDarkBg)
            .testTag("admin_panel_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MascaraRed)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Panel de Administración", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("MáscaraTV Moderación & Seguridad", color = MascaraDramaSilver, fontSize = 11.sp)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MascaraSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Videos Activos", color = MascaraTextMuted, fontSize = 11.sp)
                            Text("${videos.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("100% verificado", color = MascaraGreen, fontSize = 10.sp)
                        }
                    }

                    Surface(
                        color = MascaraSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Reportes Pendientes", color = MascaraTextMuted, fontSize = 11.sp)
                            Text("${reports.count { it.status == "Pendiente" }}", color = MascaraRed, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Requieren atención", color = MascaraGoldLight, fontSize = 10.sp)
                        }
                    }

                    Surface(
                        color = MascaraSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Estado del Sistema", color = MascaraTextMuted, fontSize = 11.sp)
                            Text("Normal", color = MascaraGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Firebase en línea", color = MascaraGreen, fontSize = 10.sp)
                        }
                    }
                }
            }

            // Reports Queue Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MascaraGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cola de Moderación (${reports.size})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            if (reports.isEmpty()) {
                item {
                    Surface(
                        color = MascaraSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨ No hay reportes pendientes de moderación.", color = MascaraGreen)
                        }
                    }
                }
            } else {
                items(reports, key = { it.id }) { report ->
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (report.type == "VIDEO") MascaraRed else MascaraGold,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = report.type,
                                            color = if (report.type == "VIDEO") Color.White else MascaraDarkBg,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Estado: ${report.status}",
                                        color = if (report.status == "Pendiente") MascaraGoldLight else MascaraGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Text(
                                    text = report.timeAgo,
                                    color = MascaraTextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Elemento: ${report.targetTitle}",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )

                            Text(
                                text = "Motivo de reporte: ${report.reason}",
                                color = MascaraDramaSilver,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Text(
                                text = "Reportado por: ${report.reporterName}",
                                color = MascaraTextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (report.status == "Pendiente") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { onDismissReport(report.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text("Desestimar", fontSize = 11.sp, color = MascaraDramaSilver)
                                    }

                                    Button(
                                        onClick = {
                                            if (report.type == "VIDEO") {
                                                onDeleteVideo(report.targetId)
                                            }
                                            onDismissReport(report.id)
                                            Toast.makeText(context, "Infracción eliminada con éxito", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MascaraRed, contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Eliminar infracción", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Category Management
            item {
                Text(
                    text = "Gestión de Categorías",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = MascaraSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Categorías activas en el carrusel de inicio:",
                            color = MascaraDramaSilver,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Comedia • Drama • Cine • Música • Gaming • Shorts • Nuevos videos • Tendencias",
                            color = MascaraGoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
