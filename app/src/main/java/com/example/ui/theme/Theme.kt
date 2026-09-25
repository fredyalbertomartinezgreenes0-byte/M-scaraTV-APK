package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MascaraColorScheme = darkColorScheme(
    primary = MascaraGold,
    onPrimary = MascaraDarkBg,
    primaryContainer = MascaraSurfaceVariant,
    onPrimaryContainer = MascaraGoldLight,
    secondary = MascaraDramaSilver,
    onSecondary = MascaraDarkBg,
    secondaryContainer = MascaraSurfaceElevated,
    onSecondaryContainer = Color.White,
    tertiary = MascaraRed,
    onTertiary = Color.White,
    background = MascaraDarkBg,
    onBackground = MascaraTextPrimary,
    surface = MascaraSurface,
    onSurface = MascaraTextPrimary,
    surfaceVariant = MascaraSurfaceVariant,
    onSurfaceVariant = MascaraTextSecondary,
    outline = MascaraSurfaceElevated,
    outlineVariant = Color(0xFF2E3D59)
)

@Composable
fun MascaraTvTheme(
    darkTheme: Boolean = true, // Force rich dark cinematic theme for MáscaraTV
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MascaraColorScheme,
        typography = Typography,
        content = content
    )
}
