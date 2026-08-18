package com.nullhorizon.app.ui.chrome

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme

/** Repair state of a region as the schematic draws it. */
enum class SchematicStatus {
    Offline,
    Degraded,
    Restored,
}

/** One module on the hull schematic. */
data class SchematicRegion(
    val id: String,
    val name: String,
    val accent: Color,
    val status: SchematicStatus,
    val completedCount: Int,
    val missionCount: Int,
)

private val RowHeight = 78.dp
private val NoseDepth = 26.dp
private val EngineDepth = 30.dp
private val SpineHalfWidth = 17.dp
private val HullInset = 2.dp

/**
 * The ship map drawn as an engineering schematic instead of a list of chips:
 * a hull outline with a power spine running fore to aft and every region
 * hanging off it on alternating sides.
 *
 * Modules are real composables (not canvas text), so each one keeps its own
 * touch target, focus stop, and screen-reader label; only the hull, spine, and
 * conduits are drawn. Restored regions light their conduit and carry a power
 * pulse down the spine; damaged ones stay dashed. All motion is gated on
 * [NhTheme] animated chrome, and every status is spelled out in text as well
 * as colour.
 */
@Composable
fun ShipSchematic(
    regions: List<SchematicRegion>,
    selectedRegionId: String?,
    onSelectRegion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (regions.isEmpty()) return
    val density = LocalDensity.current
    val animate = NhTheme.accessibility.animatedChromeEnabled
    val restoredFraction = regions.count { it.status == SchematicStatus.Restored }
        .toFloat() / regions.size.toFloat()

    val rows = (regions.size + 1) / 2
    val schematicHeight = NoseDepth + RowHeight * rows + EngineDepth

    val pulse = if (animate) {
        val transition = rememberInfiniteTransition(label = "hull-power")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
            label = "hull-power-pulse",
        ).value
    } else {
        -1f
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(schematicHeight)
            .semantics {
                contentDescription = "Ship schematic, " +
                    "${regions.count { it.status == SchematicStatus.Restored }} of " +
                    "${regions.size} regions restored"
            },
    ) {
        val totalWidth = maxWidth
        val centerX = totalWidth / 2
        val nodeWidth = centerX - SpineHalfWidth - HullInset - 14.dp
        val nodeHeight = RowHeight - 10.dp

        Canvas(modifier = Modifier.fillMaxSize()) {
            val px = { dp: Dp -> with(density) { dp.toPx() } }
            val hullLeft = px(HullInset)
            val hullRight = size.width - px(HullInset)
            val cx = size.width / 2f
            val noseY = px(NoseDepth)
            val aftY = size.height - px(EngineDepth)
            val hullColor = NhColors.PhosphorDim.copy(alpha = 0.55f)

            // Hull outline: nose cone, parallel flanks, engine flare.
            drawLine(hullColor, Offset(cx, 0f), Offset(hullLeft, noseY), 2f, StrokeCap.Round)
            drawLine(hullColor, Offset(cx, 0f), Offset(hullRight, noseY), 2f, StrokeCap.Round)
            drawLine(hullColor, Offset(hullLeft, noseY), Offset(hullLeft, aftY), 2f)
            drawLine(hullColor, Offset(hullRight, noseY), Offset(hullRight, aftY), 2f)
            drawLine(hullColor, Offset(hullLeft, aftY), Offset(cx - px(28.dp), size.height), 2f)
            drawLine(hullColor, Offset(hullRight, aftY), Offset(cx + px(28.dp), size.height), 2f)
            drawLine(
                hullColor,
                Offset(cx - px(28.dp), size.height),
                Offset(cx + px(28.dp), size.height),
                2f,
            )

            // Engine flare brightens with how much of the ship is back online.
            val exhaust = NhColors.PhosphorGreen.copy(alpha = 0.18f + 0.55f * restoredFraction)
            for (i in -2..2) {
                val x = cx + i * px(11.dp)
                drawLine(
                    exhaust,
                    Offset(x, aftY + px(4.dp)),
                    Offset(x, size.height - px(3.dp)),
                    3f,
                    StrokeCap.Round,
                )
            }

            // Power spine, drawn as a twin rail with deck rungs.
            val spineTop = noseY
            val spineBottom = aftY
            val spineColor = NhColors.PhosphorDim.copy(alpha = 0.9f)
            drawLine(
                spineColor,
                Offset(cx - px(4.dp), spineTop),
                Offset(cx - px(4.dp), spineBottom),
                2.5f,
            )
            drawLine(
                spineColor,
                Offset(cx + px(4.dp), spineTop),
                Offset(cx + px(4.dp), spineBottom),
                2.5f,
            )
            var rung = spineTop
            while (rung < spineBottom) {
                drawLine(
                    NhColors.PhosphorDim.copy(alpha = 0.3f),
                    Offset(cx - px(4.dp), rung),
                    Offset(cx + px(4.dp), rung),
                    1.5f,
                )
                rung += px(9.dp)
            }

            // Conduits from the spine out to each module.
            regions.forEachIndexed { index, region ->
                val onLeft = index % 2 == 0
                val rowTop = px(NoseDepth) + px(RowHeight) * (index / 2)
                val nodeCenterY = rowTop + px(RowHeight) / 2f
                val innerX = if (onLeft) cx - px(SpineHalfWidth) else cx + px(SpineHalfWidth)
                val outerX = if (onLeft) {
                    cx - px(SpineHalfWidth) - px(12.dp)
                } else {
                    cx + px(SpineHalfWidth) + px(12.dp)
                }
                val live = region.status == SchematicStatus.Restored
                val conduitColor = when (region.status) {
                    SchematicStatus.Restored -> region.accent
                    SchematicStatus.Degraded -> region.accent.copy(alpha = 0.55f)
                    SchematicStatus.Offline -> NhColors.PhosphorDim.copy(alpha = 0.4f)
                }
                drawLine(
                    color = conduitColor,
                    start = Offset(if (onLeft) outerX else innerX, nodeCenterY),
                    end = Offset(if (onLeft) innerX else outerX, nodeCenterY),
                    strokeWidth = if (live) 3f else 2f,
                    pathEffect = if (live) {
                        null
                    } else {
                        PathEffect.dashPathEffect(floatArrayOf(px(3.dp), px(4.dp)))
                    },
                )
                // Tap point where the conduit meets the spine.
                drawCircle(
                    color = conduitColor,
                    radius = px(if (live) 3.dp else 2.dp),
                    center = Offset(innerX, nodeCenterY),
                )
            }

            // A single charge running aft down the spine once power is flowing.
            if (pulse >= 0f && restoredFraction > 0f) {
                val y = spineTop + (spineBottom - spineTop) * pulse
                val tail = px(26.dp)
                drawLine(
                    NhColors.PhosphorHot.copy(alpha = 0.85f),
                    Offset(cx, (y - tail).coerceAtLeast(spineTop)),
                    Offset(cx, y),
                    5f,
                    StrokeCap.Round,
                )
            }
        }

        regions.forEachIndexed { index, region ->
            val onLeft = index % 2 == 0
            val rowTop = NoseDepth + RowHeight * (index / 2) + 6.dp
            val startX = if (onLeft) HullInset + 4.dp else centerX + SpineHalfWidth + 2.dp
            SchematicNode(
                region = region,
                selected = region.id == selectedRegionId,
                onClick = { onSelectRegion(region.id) },
                modifier = Modifier
                    .offset(x = startX, y = rowTop)
                    .width(nodeWidth)
                    .height(nodeHeight),
            )
        }
    }
}

@Composable
private fun SchematicNode(
    region: SchematicRegion,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusText = when (region.status) {
        SchematicStatus.Restored -> "RESTORED"
        SchematicStatus.Degraded -> "DEGRADED"
        SchematicStatus.Offline -> "OFFLINE"
    }
    val glyph = when (region.status) {
        SchematicStatus.Restored -> "●"
        SchematicStatus.Degraded -> "◐"
        SchematicStatus.Offline -> "○"
    }
    val borderColor = when {
        selected -> region.accent
        region.status == SchematicStatus.Offline -> NhColors.PhosphorDim.copy(alpha = 0.6f)
        else -> region.accent.copy(alpha = 0.75f)
    }
    val nameColor = if (selected) region.accent else NhColors.PhosphorWhite
    // Powered modules sit on a faint accent wash so live decks read as lit
    // cells on the hull rather than empty outlines.
    val fill = when (region.status) {
        SchematicStatus.Restored -> region.accent.copy(alpha = 0.14f)
        SchematicStatus.Degraded -> region.accent.copy(alpha = 0.07f)
        SchematicStatus.Offline -> Color.Transparent
    }
    Column(
        modifier = modifier
            .semantics {
                contentDescription = "Ship region ${region.name}, status $statusText, " +
                    "${region.completedCount} of ${region.missionCount} systems restored"
            }
            .clickable(onClick = onClick)
            .background(fill)
            .drawTuiBorder(color = borderColor)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            text = if (selected) "▌${region.name.uppercase()}" else region.name.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = nameColor,
            fontFamily = NhTheme.fontFamily,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "$glyph $statusText",
            style = MaterialTheme.typography.labelMedium,
            color = if (region.status == SchematicStatus.Offline) {
                NhColors.PhosphorDim
            } else {
                region.accent
            },
            fontFamily = NhTheme.fontFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = meterBar(region.completedCount, region.missionCount) +
                " ${region.completedCount}/${region.missionCount}",
            style = MaterialTheme.typography.labelMedium,
            color = NhColors.PhosphorDim,
            fontFamily = NhTheme.fontFamily,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}

/**
 * Ship-wide condition strip: hull, power, and decoded signal coverage, each as
 * a filled block meter with its percentage spelled out.
 */
@Composable
fun ShipVitalsStrip(
    hullPercent: Int,
    powerPercent: Int,
    dataPercent: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            VitalsRow(label = "HULL", percent = hullPercent)
            VitalsRow(label = "PWR ", percent = powerPercent)
            VitalsRow(label = "DATA", percent = dataPercent)
        }
    }
}

@Composable
private fun VitalsRow(label: String, percent: Int) {
    val clamped = percent.coerceIn(0, 100)
    val band = when {
        clamped >= 67 -> NhColors.PhosphorGreen
        clamped >= 34 -> NhColors.PhosphorAmber
        else -> NhColors.PhosphorRed
    }
    Text(
        text = "$label ${meterBar(clamped, 100)} ${clamped.toString().padStart(3)}%",
        style = MaterialTheme.typography.labelMedium,
        color = band,
        fontFamily = NhTheme.fontFamily,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        modifier = Modifier.semantics { contentDescription = "$label $clamped percent" },
    )
}

/** Block meter such as `▰▰▰▱▱▱▱▱`, sized to [cells] glyphs. */
internal fun meterBar(part: Int, whole: Int, cells: Int = 8): String {
    if (whole <= 0) return "▱".repeat(cells)
    val filled = ((part.toFloat() / whole.toFloat()) * cells)
        .toInt()
        .coerceIn(0, cells)
    return "▰".repeat(filled) + "▱".repeat(cells - filled)
}
