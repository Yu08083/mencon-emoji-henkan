package com.emojicode.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// === Web版と統一したパレット ===
private val InkLight = Color(0xFF1A2942)
private val InkDark = Color(0xFFEBE5D6)
private val PaperLight = Color(0xFFF4EDE0)
private val PaperDark = Color(0xFF0E1320)
private val SurfaceLight = Color(0xFFFAF6EC)
private val SurfaceDark = Color(0xFF161D2F)
private val AccentLight = Color(0xFF8B6F47)
private val AccentDark = Color(0xFFD4A85A)
private val DangerLight = Color(0xFFA8362B)
private val DangerDark = Color(0xFFD97366)
private val LineLight = Color(0xFFDDD0B3)
private val LineDark = Color(0xFF2A3550)

private val LightColors = lightColorScheme(
    primary = AccentLight,
    onPrimary = SurfaceLight,
    secondary = InkLight,
    background = PaperLight,
    onBackground = InkLight,
    surface = SurfaceLight,
    onSurface = InkLight,
    surfaceVariant = Color(0xFFF0E7D4),
    onSurfaceVariant = Color(0xFF324360),
    outline = LineLight,
    error = DangerLight,
)

private val DarkColors = darkColorScheme(
    primary = AccentDark,
    onPrimary = Color(0xFF1A1A1A),
    secondary = InkDark,
    background = PaperDark,
    onBackground = InkDark,
    surface = SurfaceDark,
    onSurface = InkDark,
    surfaceVariant = Color(0xFF1D263C),
    onSurfaceVariant = Color(0xFFC8C0AC),
    outline = LineDark,
    error = DangerDark,
)

@Composable
fun EmojiCodeTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
