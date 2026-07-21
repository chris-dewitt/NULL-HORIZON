package com.nullhorizon.app.progression

/** One decoded fragment of the Auditor mystery, revealed as repairs progress. */
data class AuditorFragment(
    val index: Int,
    val total: Int,
    val text: String,
)

/**
 * The Auditor thread: a slow-drip mystery that surfaces one signal fragment
 * each time the operator finishes a repair, giving a reason to keep going
 * beyond the mission objectives themselves. Deterministic — the fragment shown
 * is a function of how many missions are complete, so it never uses an LLM and
 * replays identically.
 */
object AuditorLog {
    private val fragments: List<String> = listOf(
        "An audit was already running when you woke. No one authorised it. It has not stopped.",
        "Crew manifest: 2,400 sleepers. Registry acknowledges 2,399. The missing entry was overwritten, not deleted.",
        "ORION's logs skip fourteen days. ORION insists the fourteen days did not happen.",
        "Every system you repair reports itself to a channel that isn't on the ship's map.",
        "The Auditor is not a fault. It is a process with credentials higher than the Commander's.",
        "You found the reserve-power drain. It routes to a compartment sealed from the inside.",
        "MICA left a note in a table you weren't meant to query: \"It grades us. Do not pass.\"",
        "The signature on the sealed orders is yours. Dated before you were revived.",
        "The Auditor has begun a new pass. This time it is auditing you.",
    )

    val total: Int = fragments.size

    /**
     * The fragment to reveal for [completedMissionCount] completed missions
     * (1-based: the first completion reveals fragment 1). Returns null before
     * any completion; caps at the final fragment once the log is exhausted.
     */
    fun fragmentFor(completedMissionCount: Int): AuditorFragment? {
        if (completedMissionCount <= 0) return null
        val index = completedMissionCount.coerceAtMost(fragments.size)
        return AuditorFragment(index = index, total = fragments.size, text = fragments[index - 1])
    }
}
