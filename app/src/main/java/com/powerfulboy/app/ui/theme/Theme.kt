package com.powerfulboy.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette
val Lime = Color(0xFFE8FF47)
val LimeDim = Color(0xFFC8E030)
val Background = Color(0xFF0D0D0D)
val Surface = Color(0xFF1A1A1A)
val SurfaceVariant = Color(0xFF242424)
val OnBackground = Color(0xFFF0F0F0)
val OnSurface = Color(0xFFD0D0D0)
val OnSurfaceMuted = Color(0xFF888888)
val ErrorRed = Color(0xFFFF5252)
val CalColor = Color(0xFFFF9500)
val ProteinColor = Color(0xFF4FC3F7)
val CarbColor = Color(0xFFE8FF47)
val FatColor = Color(0xFFCE93D8)

private val DarkColorScheme = darkColorScheme(
    primary = Lime,
    onPrimary = Color(0xFF1A1A00),
    primaryContainer = Color(0xFF2A2E00),
    onPrimaryContainer = Lime,
    secondary = LimeDim,
    onSecondary = Color(0xFF1A1A00),
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceMuted,
    error = ErrorRed,
    outline = Color(0xFF333333)
)

@Composable
fun PowerfulBoyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
