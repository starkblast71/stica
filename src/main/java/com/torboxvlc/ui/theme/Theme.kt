package com.torboxvlc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF82AAFF),
    onPrimary = Color(0xFF003399),
    primaryContainer = Color(0xFF004BC6),
    secondary = Color(0xFF5CD6C0),
    background = Color(0xFF0F1117),
    surface = Color(0xFF1A1D27),
    surfaceVariant = Color(0xFF252836),
    onBackground = Color(0xFFE2E8FF),
    onSurface = Color(0xFFE2E8FF),
    error = Color(0xFFFF6B6B)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1A56DB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF0B8A78),
    background = Color(0xFFF6F8FF),
    surface = Color.White,
    surfaceVariant = Color(0xFFEEF2FF),
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
    error = Color(0xFFCC2D2D)
)

@Composable
fun TorBoxVLCTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
