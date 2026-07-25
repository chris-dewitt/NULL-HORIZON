package com.nullhorizon.app.ui.chrome

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Blinking block cursor for terminal-style inputs. Static when animated chrome is disabled.
 */
@Composable
fun BlockCursor(
    modifier: Modifier = Modifier,
    color: Color = NhColors.PhosphorHot,
    blinkMillis: Long = 530L,
) {
    val animated = NhTheme.accessibility.animatedChromeEnabled
    var visible by remember { mutableStateOf(true) }

    if (animated) {
        LaunchedEffect(blinkMillis) {
            while (isActive) {
                delay(blinkMillis)
                visible = !visible
            }
        }
    }

    Text(
        text = if (visible || !animated) "█" else " ",
        modifier = modifier.semantics { contentDescription = "Cursor" },
        color = color,
        style = MaterialTheme.typography.bodyMedium.copy(
            shadow = Shadow(color = color, blurRadius = 14f),
        ),
        fontFamily = FontFamily.Monospace,
    )
}
