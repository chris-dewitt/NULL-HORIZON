package com.nullhorizon.app.ui.chrome

/**
 * Bounds for the static phosphor bloom permitted on chrome by ADR-0023.
 *
 * ADR-0022 originally banned bloom outright. ADR-0023 narrows that to a
 * bounded, static halo on chrome only — panel borders, titles, status glyphs,
 * meters, and nav — never on long-form body text, code, SQL, terminal output,
 * or learner errors, which must stay crisp.
 *
 * The caps live here as constants rather than as prose in the ADR so the
 * contract is testable and every bloom site draws from one definition.
 */
object PhosphorBloom {
    /** Halo passes drawn under the crisp stroke. */
    const val MAX_HALO_PASSES = 2

    /** Widest halo, as a multiple of the crisp stroke width. */
    const val MAX_HALO_WIDTH_FACTOR = 7f

    /** Alpha of the widest halo pass — a wash, never a readable edge. */
    const val OUTER_HALO_ALPHA = 0.12f

    /** Alpha of the inner halo pass. */
    const val INNER_HALO_ALPHA = 0.30f

    /** Inner halo width, as a multiple of the crisp stroke width. */
    const val INNER_HALO_WIDTH_FACTOR = 3.6f

    /** Ceiling on any text shadow blur used for chrome titles, in pixels. */
    const val MAX_TITLE_SHADOW_BLUR = 24f

    /** Blur applied to ALL-CAPS chrome titles. */
    const val TITLE_SHADOW_BLUR = 22f

    /**
     * True when a halo stays inside the ADR-0023 envelope: no more than
     * [MAX_HALO_PASSES] passes, none wider than [MAX_HALO_WIDTH_FACTOR], and
     * no halo alpha dense enough to read as a second border.
     */
    fun withinBounds(widthFactors: List<Float>, alphas: List<Float>): Boolean {
        if (widthFactors.size > MAX_HALO_PASSES) return false
        if (widthFactors.size != alphas.size) return false
        if (widthFactors.any { it > MAX_HALO_WIDTH_FACTOR }) return false
        return alphas.all { it <= INNER_HALO_ALPHA }
    }
}
