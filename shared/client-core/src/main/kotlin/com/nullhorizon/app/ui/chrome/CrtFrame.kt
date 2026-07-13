package com.nullhorizon.app.ui.chrome

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

/**
 * CRT presentation frame: scanlines + vignette, optional rare flicker.
 * Disabled when high-contrast or reduced-motion accessibility prefs are on.
 */
@Composable
fun CrtFrame(
    modifier: Modifier = Modifier,
    enableFlicker: Boolean = true,
    content: @Composable () -> Unit,
) {
    val a11y = NhTheme.accessibility
    val effects = a11y.crtEffectsEnabled
    var flickerAlpha by remember { mutableFloatStateOf(0f) }

    if (effects && enableFlicker && a11y.animatedChromeEnabled) {
        LaunchedEffect(Unit) {
            while (isActive) {
                delay(Random.nextLong(4_000L, 9_000L))
                flickerAlpha = 0.08f
                delay(40)
                flickerAlpha = 0f
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (effects) {
                    Modifier.drawWithContent {
                        drawContent()
                        val step = 3f
                        var y = 0f
                        while (y < size.height) {
                            drawLine(
                                color = NhColors.Scanline,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1f,
                            )
                            y += step
                        }
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Transparent, NhColors.Vignette),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.minDimension * 0.85f,
                            ),
                            size = Size(size.width, size.height),
                        )
                        if (flickerAlpha > 0f) {
                            drawRect(Color.White.copy(alpha = flickerAlpha))
                        }
                    }
                } else {
                    Modifier
                },
            ),
    ) {
        content()
    }
}
