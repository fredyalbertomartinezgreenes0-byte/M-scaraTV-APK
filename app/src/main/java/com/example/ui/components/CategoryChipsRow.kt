package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import com.example.ui.theme.MascaraDarkBg
import com.example.ui.theme.MascaraDramaSilver
import com.example.ui.theme.MascaraGold
import com.example.ui.theme.MascaraSurfaceElevated
import com.example.ui.theme.MascaraSurfaceVariant

val MASCARA_CATEGORIES = listOf(
    "Para ti",
    "Tendencias",
    "Nuevos videos",
    "Comedia",
    "Drama",
    "Música",
    "Gaming",
    "Shorts",
    "Cine",
    "Creadores"
)

@Composable
fun CategoryChipsRow(
    modifier: Modifier = Modifier,
    categories: List<String> = MASCARA_CATEGORIES,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(MascaraDarkBg)
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) MascaraGold else MascaraSurfaceVariant,
                label = "chip_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) MascaraDarkBg else Color.White,
                label = "chip_text"
            )

            val emoji = when (category) {
                "Para ti" -> "✨"
                "Tendencias" -> "🔥"
                "Nuevos videos" -> "🆕"
                "Comedia" -> "🎭"
                "Drama" -> "🎭"
                "Música" -> "🎵"
                "Gaming" -> "🎮"
                "Shorts" -> "⚡"
                "Cine" -> "🎬"
                "Creadores" -> "⭐"
                else -> "📺"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .then(
                        if (!isSelected) Modifier.border(
                            1.dp,
                            MascaraSurfaceElevated,
                            RoundedCornerShape(20.dp)
                        ) else Modifier
                    )
                    .clickable { onSelectCategory(category) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("category_chip_${category.lowercase().replace(" ", "_")}"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = emoji, fontSize = 13.sp)
                    Text(
                        text = " $category",
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
