package com.nullhorizon.app.ui.chrome

/**
 * CRT overlay intensity profiles (ADR-0022 / DESIGN_SYSTEM §6).
 *
 * [Medium] — PC default: denser scanlines and stronger vignette.
 * [Lean] — Android phones: lighter scanlines and softer vignette.
 */
data class CrtProfile(
    val name: String,
    val scanlineStepPx: Float,
    val scanlineAlpha: Float,
    val vignetteStrength: Float,
) {
    companion object {
        val Medium = CrtProfile(
            name = "medium",
            scanlineStepPx = 2.5f,
            scanlineAlpha = 0.10f,
            vignetteStrength = 0.46f,
        )

        val Lean = CrtProfile(
            name = "lean",
            scanlineStepPx = 3.5f,
            scanlineAlpha = 0.045f,
            vignetteStrength = 0.30f,
        )
    }
}
