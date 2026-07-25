package com.nullhorizon.app.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * TUI-style panel with box-drawing border and ALL-CAPS title.
 *
 * Visual target: `┌─ SYSTEMS ─┐` … `└───────────┘`
 */
@Composable
fun TuiPanel(
    title: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.outline,
    contentPadding: Dp = 10.dp,
    content: @Composable () -> Unit,
) {
    val normalizedTitle = title.trim().uppercase().ifEmpty { "PANEL" }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to accent.copy(alpha = 0.14f),
                    0.5f to Color.Transparent,
                ),
            )
            .semantics { contentDescription = "Panel $normalizedTitle" }
            .drawTuiBorder(color = accent),
    ) {
        Text(
            text = "┌─ $normalizedTitle ─┐",
            style = MaterialTheme.typography.labelMedium.copy(
                shadow = Shadow(color = accent, blurRadius = 22f),
            ),
            color = accent,
            fontFamily = NhTheme.fontFamily,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.padding(start = 6.dp, top = 2.dp),
        )
        // Bottom corner glyphs complete the box-drawing frame.
        Text(
            text = "└",
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontFamily = NhTheme.fontFamily,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 6.dp, bottom = 1.dp),
        )
        Text(
            text = "┘",
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontFamily = NhTheme.fontFamily,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 6.dp, bottom = 1.dp),
        )
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(top = 10.dp),
        ) {
            content()
        }
    }
}

/**
 * Compact selectable region cell using box-drawing chrome.
 */
@Composable
fun TuiRegionChip(
    name: String,
    status: String,
    selected: Boolean,
    accent: Color,
    onClickLabel: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) accent else NhColors.PhosphorDim
    val normalizedStatus = status.uppercase()
    val isRestored = normalizedStatus.contains("RESTORED") ||
        normalizedStatus.contains("COMPLETED") ||
        normalizedStatus.contains("ONLINE") ||
        normalizedStatus.contains("OK")
    val isOffline = normalizedStatus.contains("OFFLINE") ||
        normalizedStatus.contains("LOCKED") ||
        normalizedStatus.contains("BLOCKED")
    val isDamaged = !isRestored && (
        isOffline ||
            normalizedStatus.contains("DEGRADED") ||
            normalizedStatus.contains("FAILED") ||
            normalizedStatus.contains("ERROR")
        )
    val statusGlyph = when {
        isRestored -> "●"
        isOffline -> "○"
        isDamaged -> "◐"
        else -> "●"
    }
    // Damaged regions flicker (unstable); restored regions breathe slowly
    // (powered/alive). Both respect reduced motion.
    val animatedStatus =
        (isDamaged || isRestored) && NhTheme.accessibility.animatedChromeEnabled
    var statusAlpha by remember(normalizedStatus, animatedStatus) { mutableFloatStateOf(1f) }

    LaunchedEffect(normalizedStatus, animatedStatus, isDamaged) {
        if (!animatedStatus) {
            statusAlpha = 1f
            return@LaunchedEffect
        }
        if (isDamaged) {
            while (isActive) {
                statusAlpha = 0.58f
                delay(180)
                statusAlpha = 1f
                delay(920)
            }
        } else {
            while (isActive) {
                statusAlpha = 1f
                delay(720)
                statusAlpha = 0.9f
                delay(300)
                statusAlpha = 0.8f
                delay(300)
                statusAlpha = 0.9f
                delay(300)
            }
        }
    }

    Column(
        modifier = modifier
            .semantics { contentDescription = onClickLabel }
            .clickable(onClick = onClick)
            .drawTuiBorder(color = borderColor)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) accent else NhColors.PhosphorWhite,
            fontFamily = NhTheme.fontFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "$statusGlyph $normalizedStatus",
            style = MaterialTheme.typography.labelMedium,
            color = (if (selected) accent else NhColors.PhosphorDim).copy(alpha = statusAlpha),
            fontFamily = NhTheme.fontFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

fun Modifier.drawTuiBorder(
    color: Color,
    strokeWidth: Float = 1.5f,
): Modifier = drawBehind {
    val inset = 3f
    val left = inset
    val top = inset
    val right = size.width - inset
    val bottom = size.height - inset

    // Each segment of the box-drawing frame, drawn once per pass.
    fun frame(sw: Float, c: Color) {
        // Top and bottom
        drawLine(c, Offset(left + 8f, top), Offset(right - 8f, top), sw, StrokeCap.Square)
        drawLine(c, Offset(left + 8f, bottom), Offset(right - 8f, bottom), sw, StrokeCap.Square)
        // Sides
        drawLine(c, Offset(left, top + 8f), Offset(left, bottom - 8f), sw, StrokeCap.Square)
        drawLine(c, Offset(right, top + 8f), Offset(right, bottom - 8f), sw, StrokeCap.Square)
        // Corner ticks approximating box-drawing corners
        drawLine(c, Offset(left, top + 8f), Offset(left, top), sw, StrokeCap.Square)
        drawLine(c, Offset(left, top), Offset(left + 8f, top), sw, StrokeCap.Square)
        drawLine(c, Offset(right, top + 8f), Offset(right, top), sw, StrokeCap.Square)
        drawLine(c, Offset(right, top), Offset(right - 8f, top), sw, StrokeCap.Square)
        drawLine(c, Offset(left, bottom - 8f), Offset(left, bottom), sw, StrokeCap.Square)
        drawLine(c, Offset(left, bottom), Offset(left + 8f, bottom), sw, StrokeCap.Square)
        drawLine(c, Offset(right, bottom - 8f), Offset(right, bottom), sw, StrokeCap.Square)
        drawLine(c, Offset(right, bottom), Offset(right - 8f, bottom), sw, StrokeCap.Square)
    }

    // Fat phosphor bloom: wide halo, inner bloom, then a bold crisp line on top.
    frame(strokeWidth * 7f, color.copy(alpha = 0.12f))
    frame(strokeWidth * 3.6f, color.copy(alpha = 0.30f))
    frame(strokeWidth * 1.5f, color)
}
