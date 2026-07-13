package com.nullhorizon.pc.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import com.nullhorizon.app.ui.theme.NhFontFamilyFallback

/**
 * Loads the Terminal face (VT323, SIL OFL) from classpath resources.
 * Falls back to monospace if the resource is missing.
 */
@Composable
fun rememberTerminalFontFamily(): FontFamily = remember {
    runCatching {
        FontFamily(
            Font(
                resource = "fonts/NhTerminal-Regular.ttf",
                weight = FontWeight.Normal,
            ),
        )
    }.getOrElse { NhFontFamilyFallback }
}
