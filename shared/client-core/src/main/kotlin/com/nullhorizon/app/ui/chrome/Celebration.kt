package com.nullhorizon.app.ui.chrome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme
import kotlin.math.roundToInt

/**
 * Visual celebration chrome for mission wins — shared by both clients so a
 * completed repair looks the same everywhere. Every animation is gated on
 * [NhTheme] accessibility (reduced motion), so nothing here fights the
 * accessibility settings. Haptics are Android-only (see the app module).
 */

/**
 * Integer that animates up from zero to [target] for reward reveals. Returns
 * [target] immediately when reduced motion is enabled.
 */
@Composable
fun animatedCount(target: Int, durationMillis: Int = 900): Int {
    val animate = NhTheme.accessibility.animatedChromeEnabled
    val value = remember(target) { Animatable(if (animate) 0f else target.toFloat()) }
    LaunchedEffect(target, animate) {
        if (animate) {
            value.snapTo(0f)
            value.animateTo(target.toFloat(), tween(durationMillis))
        } else {
            value.snapTo(target.toFloat())
        }
    }
    return value.value.roundToInt()
}

/**
 * Rank-up ceremony banner. Pulses its accent when motion is allowed; announces
 * itself to screen readers as a live region either way.
 */
@Composable
fun RankUpBanner(
    title: String,
    previousRank: String,
    newRank: String,
    modifier: Modifier = Modifier,
) {
    val animate = NhTheme.accessibility.animatedChromeEnabled
    val pulse = if (animate) {
        val transition = rememberInfiniteTransition(label = "rankup")
        transition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
            label = "rankup-alpha",
        ).value
    } else {
        1f
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawTuiBorder(color = NhColors.PhosphorAmber)
            .padding(12.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = "$title: $previousRank to $newRank"
            },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = NhColors.PhosphorAmber,
            fontFamily = NhTheme.fontFamily,
            modifier = Modifier.alpha(pulse),
        )
        Text(
            text = "${previousRank.uppercase()}  ▸  ${newRank.uppercase()}",
            style = MaterialTheme.typography.bodyLarge,
            color = NhColors.PhosphorWhite,
            fontFamily = NhTheme.fontFamily,
        )
    }
}
