package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenPrimaryLight,
    onPrimaryContainer = GreenPrimary,
    secondary = AmberAccent,
    onSecondary = Color.White,
    secondaryContainer = AmberAccentLight,
    onSecondaryContainer = AmberAccent,
    background = WarmCanvasBg,
    onBackground = TextDark,
    surface = WarmSurface,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = TextMuted,
    outline = BorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF003913),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFA5D6A7),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF451A03),
    background = Color(0xFF121412),
    onBackground = Color(0xFFE2E3E0),
    surface = Color(0xFF1A1C1A),
    onSurface = Color(0xFFE2E3E0),
    surfaceVariant = Color(0xFF2C2F2C),
    onSurfaceVariant = Color(0xFFC2C8C2),
    outline = Color(0xFF424942)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
