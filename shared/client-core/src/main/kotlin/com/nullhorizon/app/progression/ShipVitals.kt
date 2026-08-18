package com.nullhorizon.app.progression

/**
 * Ship-wide condition readout derived from repair progress.
 *
 * Pure and deterministic so both clients show the same numbers and the rules
 * can be unit tested without Compose. Nothing here is stored: the vitals are a
 * projection of completed missions, so they can never drift from progress.
 */
/** One region's contribution to the ship readout. */
data class RegionProgress(val completed: Int, val total: Int)

data class ShipVitals(
    val missionsCompleted: Int,
    val missionsTotal: Int,
    val regionsRestored: Int,
    val regionsTotal: Int,
    val signalsDecoded: Int,
    val signalsTotal: Int,
) {
    /** Overall repair coverage across every authored mission. */
    val hullPercent: Int = percent(missionsCompleted, missionsTotal)

    /** Power comes back only when a whole region is restored, not per mission. */
    val powerPercent: Int = percent(regionsRestored, regionsTotal)

    /** How much of the Auditor thread has been decoded. */
    val dataPercent: Int = percent(signalsDecoded, signalsTotal)

    companion object {
        fun from(
            regions: List<RegionProgress>,
            signalsDecoded: Int,
            signalsTotal: Int,
        ): ShipVitals {
            val counted = regions.filter { it.total > 0 }
            return ShipVitals(
                missionsCompleted = counted.sumOf { it.completed },
                missionsTotal = counted.sumOf { it.total },
                regionsRestored = counted.count { it.completed >= it.total },
                regionsTotal = counted.size,
                signalsDecoded = signalsDecoded.coerceAtLeast(0),
                signalsTotal = signalsTotal.coerceAtLeast(0),
            )
        }

        private fun percent(part: Int, whole: Int): Int {
            if (whole <= 0) return 0
            val ratio = part.toDouble() / whole.toDouble()
            return (ratio * 100).toInt().coerceIn(0, 100)
        }
    }
}

/** Severity band for a vitals gauge, so colour is never the only signal. */
enum class VitalsBand {
    Critical,
    Degraded,
    Nominal,
    ;

    companion object {
        fun forPercent(percent: Int): VitalsBand = when {
            percent >= 67 -> Nominal
            percent >= 34 -> Degraded
            else -> Critical
        }
    }
}
