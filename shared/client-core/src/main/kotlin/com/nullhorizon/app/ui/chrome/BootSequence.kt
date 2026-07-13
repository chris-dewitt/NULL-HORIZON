package com.nullhorizon.app.ui.chrome

/**
 * Deterministic NULL HORIZON OS boot lines for launch theatre.
 */
object BootSequence {
    const val OS_VERSION: String = "NULL HORIZON OS v0.1"

    data class Line(
        val text: String,
        val delayMillis: Long = 280L,
    )

    fun defaultLines(): List<Line> = listOf(
        Line(OS_VERSION, delayMillis = 400L),
        Line("EMERGENCY INTERFACE …… INIT", delayMillis = 320L),
        Line("MEMORY CHECK ………… OK", delayMillis = 320L),
        Line("REGION MAP …………… DEGRADED", delayMillis = 320L),
        Line("OPERATOR PROFILE …… AWAIT", delayMillis = 280L),
        Line("SYSTEMS ……………… ONLINE", delayMillis = 400L),
    )
}
