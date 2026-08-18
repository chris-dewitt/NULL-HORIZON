package com.nullhorizon.app.ui.chrome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.nullhorizon.app.audio.GameSound
import com.nullhorizon.app.audio.LocalSoundPlayer
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme

/**
 * Accent colour for a workspace panel that reacts when its last operation
 * failed: the border shifts toward critical red and decays back, and an error
 * tone plays once per failure.
 *
 * A rejected command used to produce silent stderr text with no change in the
 * chrome, so a typo and a working command looked identical at a glance. This
 * gives failure a body without punishing the player — the panel settles back
 * to its resting accent on its own.
 *
 * [failureKey] must change on each new failure (a command counter, the failing
 * command string, the error text). A null key means the last operation
 * succeeded and nothing fires. The tone is independent of the animation, so
 * reduced-motion users still hear the failure; the colour shift is skipped.
 */
@Composable
fun failureAccent(
    failureKey: Any?,
    restingAccent: Color,
): Color {
    val soundPlayer = LocalSoundPlayer.current
    val animate = NhTheme.accessibility.animatedChromeEnabled
    val flash = remember { Animatable(0f) }

    LaunchedEffect(failureKey) {
        if (failureKey == null) return@LaunchedEffect
        soundPlayer.play(GameSound.Error)
        if (animate) {
            flash.snapTo(1f)
            flash.animateTo(0f, tween(1100))
        } else {
            flash.snapTo(0f)
        }
    }

    return lerpColor(restingAccent, NhColors.PhosphorRed, flash.value)
}
