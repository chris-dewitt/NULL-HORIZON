package com.nullhorizon.app.ui.chrome

/**
 * CRT overlay intensity profiles (ADR-0021 / DESIGN_SYSTEM §6).
 *
 * [Medium] — PC default: denser scanlines, stronger barrel vignette/curvature, rare flicker.
 * [Lean] — Android phones: lighter overlay, milder curve, no idle flicker.
 */
data class CrtProfile(
    val name: String,
    val scanlineStepPx: Float,
    val scanlineAlpha: Float,
    val vignetteStrength: Float,
    val barrelStrength: Float,
    val bezelFraction: Float,
    val cornerRadiusFraction: Float,
    val bloomAlpha: Float,
    val enableIdleFlicker: Boolean,
) {
    companion object {
        val Medium = CrtProfile(
            name = "medium",
            scanlineStepPx = 2.5f,
            scanlineAlpha = 0.14f,
            vignetteStrength = 0.72f,
            barrelStrength = 0.55f,
            bezelFraction = 0.028f,
            cornerRadiusFraction = 0.08f,
            bloomAlpha = 0.10f,
            enableIdleFlicker = true,
        )

        val Lean = CrtProfile(
            name = "lean",
            scanlineStepPx = 3.5f,
            scanlineAlpha = 0.07f,
            vignetteStrength = 0.45f,
            barrelStrength = 0.28f,
            bezelFraction = 0.018f,
            cornerRadiusFraction = 0.05f,
            bloomAlpha = 0.04f,
            enableIdleFlicker = false,
        )
    }
}
