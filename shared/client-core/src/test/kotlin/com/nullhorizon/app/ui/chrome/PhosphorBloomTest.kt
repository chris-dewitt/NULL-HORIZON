package com.nullhorizon.app.ui.chrome

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Guards the ADR-0023 bloom envelope. These are the numbers the ADR promises;
 * if a future effect wants to exceed them, the ADR has to move first.
 */
class PhosphorBloomTest {
    @Test
    fun defaultHalo_staysInsideTheAdrEnvelope() {
        val withinBounds = PhosphorBloom.withinBounds(
            widthFactors = listOf(
                PhosphorBloom.MAX_HALO_WIDTH_FACTOR,
                PhosphorBloom.INNER_HALO_WIDTH_FACTOR,
            ),
            alphas = listOf(PhosphorBloom.OUTER_HALO_ALPHA, PhosphorBloom.INNER_HALO_ALPHA),
        )
        assertThat(withinBounds).isTrue()
    }

    @Test
    fun haloFades_outerWashIsLighterThanInner() {
        assertThat(PhosphorBloom.OUTER_HALO_ALPHA).isLessThan(PhosphorBloom.INNER_HALO_ALPHA)
        assertThat(PhosphorBloom.INNER_HALO_WIDTH_FACTOR)
            .isLessThan(PhosphorBloom.MAX_HALO_WIDTH_FACTOR)
    }

    @Test
    fun titleShadow_staysUnderTheBlurCeiling() {
        assertThat(PhosphorBloom.TITLE_SHADOW_BLUR)
            .isAtMost(PhosphorBloom.MAX_TITLE_SHADOW_BLUR)
    }

    @Test
    fun tooManyPassesOrTooWideAHaloIsRejected() {
        assertThat(
            PhosphorBloom.withinBounds(
                widthFactors = listOf(7f, 5f, 3f),
                alphas = listOf(0.1f, 0.2f, 0.3f),
            ),
        ).isFalse()

        assertThat(
            PhosphorBloom.withinBounds(widthFactors = listOf(12f), alphas = listOf(0.1f)),
        ).isFalse()

        assertThat(
            PhosphorBloom.withinBounds(widthFactors = listOf(4f), alphas = listOf(0.8f)),
        ).isFalse()
    }
}
