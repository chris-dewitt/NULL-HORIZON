package com.nullhorizon.app.ui.chrome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.audio.GameSound
import com.nullhorizon.app.audio.LocalSoundPlayer
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme

/** One directive on the mission checklist. */
data class ObjectiveRow(
    val id: String,
    val description: String,
    val complete: Boolean,
)

/**
 * The mission directive list, with a visible moment when a directive clears.
 *
 * Completing an objective used to be a colour change and nothing else, which
 * meant the most frequent success in the game — several times per mission —
 * had no feedback at all. A newly cleared row now flashes its accent, strikes
 * its text, ticks the progress meter, plays a confirm tone, and announces
 * itself to screen readers.
 *
 * The flash is driven by comparing against the previously seen completed set,
 * so it fires on the transition rather than on every recomposition, and it is
 * skipped entirely for rows that were already complete when the screen opened
 * (returning to a finished mission should not replay its whole checklist).
 *
 * [onObjectiveCleared] lets the host add platform feedback the shared module
 * cannot reach — Android uses it for haptics.
 */
@Composable
fun ObjectiveChecklist(
    objectives: List<ObjectiveRow>,
    title: String,
    modifier: Modifier = Modifier,
    accent: Color = NhColors.PhosphorAmber,
    onObjectiveCleared: (String) -> Unit = {},
) {
    val soundPlayer = LocalSoundPlayer.current
    val animate = NhTheme.accessibility.animatedChromeEnabled
    val cleared = objectives.filter { it.complete }.map { it.id }.toSet()

    // Seeded with whatever was already complete on first composition, so a
    // resumed mission does not fire a burst of stale clears.
    val seen = remember { mutableSetOf<String>().apply { addAll(cleared) } }
    var justCleared by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(cleared) {
        val fresh = cleared - seen
        if (fresh.isNotEmpty()) {
            justCleared = fresh
            seen.addAll(fresh)
            soundPlayer.play(GameSound.Click)
            fresh.forEach(onObjectiveCleared)
        }
        // An objective can go back to incomplete when a mission is reset, so
        // drop it from the seen set and let it flash again next time.
        seen.retainAll(cleared)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "${title.uppercase()}  ${meterBar(cleared.size, objectives.size)} " +
                "${cleared.size}/${objectives.size}",
            style = MaterialTheme.typography.titleMedium,
            color = accent,
            fontFamily = NhTheme.fontFamily,
        )
        // Selectable so players can long-press to copy a path or filename out
        // of a directive and paste it straight into a terminal command.
        SelectionContainer {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                objectives.forEach { objective ->
                    ObjectiveRowItem(
                        objective = objective,
                        flash = animate && objective.id in justCleared,
                        accent = accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun ObjectiveRowItem(
    objective: ObjectiveRow,
    flash: Boolean,
    accent: Color,
) {
    // 0 at rest, 1 at the peak of a clear flash; decays back to rest.
    val highlight = remember(objective.id) { Animatable(0f) }
    LaunchedEffect(flash) {
        if (flash) {
            highlight.snapTo(1f)
            highlight.animateTo(0f, tween(900))
        } else {
            highlight.snapTo(0f)
        }
    }
    val pulse = highlight.value
    val done = objective.complete
    val borderColor = if (done) NhColors.PhosphorGreen else NhColors.PhosphorDim
    Text(
        text = "${if (done) "[x]" else "[ ]"} ${objective.description}",
        style = MaterialTheme.typography.bodyMedium,
        color = if (done) NhColors.PhosphorGreen else NhColors.PhosphorWhite,
        fontFamily = NhTheme.fontFamily,
        textDecoration = if (done) TextDecoration.LineThrough else null,
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = 0.22f * pulse))
            .drawTuiBorder(
                color = lerpColor(borderColor, accent, pulse),
                strokeWidth = 1.5f + 2.5f * pulse,
            )
            .padding(8.dp)
            .semantics {
                if (flash) {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = "Directive cleared: ${objective.description}"
                }
            },
    )
}

/** Straight-line blend between two colours; [t] is clamped to 0..1. */
internal fun lerpColor(from: Color, to: Color, t: Float): Color {
    val f = t.coerceIn(0f, 1f)
    return Color(
        red = from.red + (to.red - from.red) * f,
        green = from.green + (to.green - from.green) * f,
        blue = from.blue + (to.blue - from.blue) * f,
        alpha = from.alpha + (to.alpha - from.alpha) * f,
    )
}
