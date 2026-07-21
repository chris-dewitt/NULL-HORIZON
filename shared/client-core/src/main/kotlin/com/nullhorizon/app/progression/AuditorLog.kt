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
 * beyond the mission objectives themselves.
 *
 * The fragment copy lives in the content bundle (`content/signals/…`), loaded
 * through [com.nullhorizon.app.content.ContentRepository] so writers and
 * localization can edit it without touching platform code. This object only
 * holds the canonical signal id and the deterministic reveal logic: the
 * fragment shown is a pure function of how many missions are complete, so it
 * never uses an LLM and replays identically.
 */
object AuditorLog {
    /** Canonical signal id for the Auditor thread in the content bundle. */
    const val SIGNAL_ID: String = "auditor.thread.01"

    /**
     * The fragment to reveal for [completedMissionCount] completed missions
     * (1-based: the first completion reveals fragment 1), drawn from the ordered
     * [fragments] loaded from content. Returns null before any completion or
     * when no fragments are available; caps at the final fragment once the log
     * is exhausted.
     */
    fun fragmentFor(fragments: List<String>, completedMissionCount: Int): AuditorFragment? {
        if (completedMissionCount <= 0 || fragments.isEmpty()) return null
        val index = completedMissionCount.coerceAtMost(fragments.size)
        return AuditorFragment(index = index, total = fragments.size, text = fragments[index - 1])
    }
}
