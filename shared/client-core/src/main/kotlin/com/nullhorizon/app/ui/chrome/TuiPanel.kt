package com.nullhorizon.app.ui.chrome

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
            .semantics { contentDescription = "Panel $normalizedTitle" }
            .drawTuiBorder(color = accent),
    ) {
        Text(
            text = "┌─ $normalizedTitle ─┐",
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontFamily = NhTheme.fontFamily,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.padding(start = 6.dp, top = 2.dp),
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
    val animatedStatus = isDamaged && NhTheme.accessibility.animatedChromeEnabled
    var statusAlpha by remember(normalizedStatus, animatedStatus) { mutableFloatStateOf(1f) }

    LaunchedEffect(normalizedStatus, animatedStatus) {
        if (!animatedStatus) {
            statusAlpha = 1f
            return@LaunchedEffect
        }
        while (isActive) {
            statusAlpha = 0.58f
            delay(180)
            statusAlpha = 1f
            delay(920)
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
    // Top and bottom
    drawLine(color, Offset(left + 8f, top), Offset(right - 8f, top), strokeWidth, StrokeCap.Square)
    drawLine(color, Offset(left + 8f, bottom), Offset(right - 8f, bottom), strokeWidth, StrokeCap.Square)
    // Sides
    drawLine(color, Offset(left, top + 8f), Offset(left, bottom - 8f), strokeWidth, StrokeCap.Square)
    drawLine(color, Offset(right, top + 8f), Offset(right, bottom - 8f), strokeWidth, StrokeCap.Square)
    // Corner ticks approximating box-drawing corners
    drawLine(color, Offset(left, top + 8f), Offset(left, top), strokeWidth, StrokeCap.Square)
    drawLine(color, Offset(left, top), Offset(left + 8f, top), strokeWidth, StrokeCap.Square)
    drawLine(color, Offset(right, top + 8f), Offset(right, top), strokeWidth, StrokeCap.Square)
    drawLine(color, Offset(right, top), Offset(right - 8f, top), strokeWidth, StrokeCap.Square)
    drawLine(color, Offset(left, bottom - 8f), Offset(left, bottom), strokeWidth, StrokeCap.Square)
    drawLine(color, Offset(left, bottom), Offset(left + 8f, bottom), strokeWidth, StrokeCap.Square)
    drawLine(color, Offset(right, bottom - 8f), Offset(right, bottom), strokeWidth, StrokeCap.Square)
    drawLine(color, Offset(right, bottom), Offset(right - 8f, bottom), strokeWidth, StrokeCap.Square)
}
