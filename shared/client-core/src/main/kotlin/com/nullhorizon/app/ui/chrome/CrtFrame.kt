package com.nullhorizon.app.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.compositeOver
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme
import kotlin.math.sqrt

/**
 * CRT presentation frame: scanlines plus vignette only.
 *
 * Disabled when Disable CRT or high contrast is on.
 */
@Composable
fun CrtFrame(
    modifier: Modifier = Modifier,
    profile: CrtProfile = CrtProfile.Medium,
    content: @Composable () -> Unit,
) {
    val a11y = NhTheme.accessibility
    val effects = a11y.crtEffectsEnabled

    if (!effects) {
        Box(modifier = modifier.fillMaxSize()) {
            content()
        }
        return
    }

    val gridColor = NhColors.PhosphorGreen.copy(alpha = 0.10f)
    val groundGlow = NhColors.PhosphorGreen.copy(alpha = 0.10f)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to groundGlow.compositeOver(NhColors.CrtRaised),
                    0.55f to NhColors.CrtBlack,
                    1.0f to NhColors.CrtBlack,
                ),
            )
            .drawWithContent {
                // Reactor grid sits under the content for physical depth.
                val step = 32f
                var gx = 0f
                while (gx < size.width) {
                    drawLine(gridColor, Offset(gx, 0f), Offset(gx, size.height), 1f)
                    gx += step
                }
                var gy = 0f
                while (gy < size.height) {
                    drawLine(gridColor, Offset(0f, gy), Offset(size.width, gy), 1f)
                    gy += step
                }

                drawContent()

                val scanColor = Color.White.copy(alpha = profile.scanlineAlpha)
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = scanColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                    )
                    y += profile.scanlineStepPx
                }

                val cx = size.width / 2f
                val cy = size.height / 2f
                val maxR = sqrt(cx * cx + cy * cy)
                drawRect(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.58f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = profile.vignetteStrength),
                        ),
                        center = Offset(cx, cy),
                        radius = maxR,
                        tileMode = TileMode.Clamp,
                    ),
                )
            },
    ) {
        content()
    }
}
