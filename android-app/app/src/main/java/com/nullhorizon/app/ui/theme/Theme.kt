package com.nullhorizon.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Graphite = Color(0xFF1B1E24)
private val WarmOffWhite = Color(0xFFE8E2D6)
private val MutedWarning = Color(0xFFB08D57)

private val ColorScheme = darkColorScheme(
    primary = MutedWarning,
    onPrimary = Graphite,
    background = Graphite,
    onBackground = WarmOffWhite,
    surface = Color(0xFF2A3038),
    onSurface = WarmOffWhite,
)

@Composable
fun NullHorizonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content = content,
    )
}
