package com.nullhorizon.app.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * CRT presentation frame: curved glass face, scanlines, barrel vignette, bloom.
 *
 * Disabled when Disable CRT or high contrast is on.
 * Idle flicker respects reduced motion and [CrtProfile.enableIdleFlicker].
 */
@Composable
fun CrtFrame(
    modifier: Modifier = Modifier,
    profile: CrtProfile = CrtProfile.Medium,
    content: @Composable () -> Unit,
) {
    val a11y = NhTheme.accessibility
    val effects = a11y.crtEffectsEnabled
    var flickerAlpha by remember(profile.name) { mutableFloatStateOf(0f) }

    val flickerAllowed =
        effects && profile.enableIdleFlicker && a11y.animatedChromeEnabled
    if (flickerAllowed) {
        LaunchedEffect(profile.name) {
            while (isActive) {
                delay(Random.nextLong(5_000L, 11_000L))
                flickerAlpha = 0.10f
                delay(35)
                flickerAlpha = 0f
            }
        }
    }

    if (!effects) {
        Box(modifier = modifier.fillMaxSize()) {
            content()
        }
        return
    }

    val bezelPad = (profile.bezelFraction * 48f).coerceIn(6f, 18f).dp
    val cornerPercent = (profile.cornerRadiusFraction * 100f).toInt().coerceIn(4, 16)
    val faceShape = RoundedCornerShape(percent = cornerPercent)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NhColors.CrtBlack)
            .padding(bezelPad)
            // Slight bulbous scale — geometric cue for curved CRT glass.
            .graphicsLayer {
                scaleX = 1f - profile.barrelStrength * 0.012f
                scaleY = 1f - profile.barrelStrength * 0.018f
                cameraDistance = 18f * density
                shadowElevation = 0f
            }
            .clip(faceShape)
            .drawWithContent {
                drawContent()

                // Soft phosphor bloom at glass rim
                if (profile.bloomAlpha > 0f) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                NhColors.PhosphorGreen.copy(alpha = profile.bloomAlpha),
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.minDimension * 0.72f,
                        ),
                    )
                }

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
                            (1f - profile.barrelStrength * 0.55f).coerceIn(0.35f, 0.85f) to
                                Color.Transparent,
                            1.0f to Color.Black.copy(alpha = profile.vignetteStrength),
                        ),
                        center = Offset(cx, cy),
                        radius = maxR,
                        tileMode = TileMode.Clamp,
                    ),
                )

                // Corner pinch reads as bulbous glass curvature.
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = profile.barrelStrength * 0.40f),
                        ),
                        center = Offset(cx, cy),
                        radius = size.minDimension * (0.70f - profile.barrelStrength * 0.10f),
                    ),
                )

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.07f),
                            Color.Transparent,
                        ),
                        startY = 0f,
                        endY = size.height * 0.20f,
                    ),
                )

                if (flickerAlpha > 0f) {
                    drawRect(Color.White.copy(alpha = flickerAlpha))
                }
            },
    ) {
        content()
    }
}
