package com.nullhorizon.app.ui.chrome

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.nullhorizon.app.ui.theme.NhTheme
import kotlinx.coroutines.delay

/**
 * Typewriter reveal for ORION/MICA dialogue. Instant when reduced motion is on.
 */
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    charsPerSecond: Int = 42,
    onFinished: (() -> Unit)? = null,
) {
    val reducedMotion = NhTheme.accessibility.reducedMotion
    var visibleChars by remember(text, reducedMotion) {
        mutableIntStateOf(if (reducedMotion) text.length else 0)
    }

    LaunchedEffect(text, reducedMotion, charsPerSecond) {
        if (reducedMotion) {
            visibleChars = text.length
            onFinished?.invoke()
            return@LaunchedEffect
        }
        visibleChars = 0
        if (text.isEmpty()) {
            onFinished?.invoke()
            return@LaunchedEffect
        }
        val delayMs = (1000L / charsPerSecond.coerceAtLeast(1)).coerceAtLeast(8L)
        while (visibleChars < text.length) {
            delay(delayMs)
            visibleChars += 1
        }
        onFinished?.invoke()
    }

    Text(
        text = text.take(visibleChars),
        modifier = modifier,
        color = color,
        style = style,
    )
}
