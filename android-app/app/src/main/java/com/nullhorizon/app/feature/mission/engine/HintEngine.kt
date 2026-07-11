package com.nullhorizon.app.feature.mission.engine

import com.nullhorizon.app.content.model.HintDefinition

class HintEngine {
    fun nextLevel(currentLevel: Int, maxLevel: Int): Int {
        if (maxLevel <= 0) return 0
        return (currentLevel + 1).coerceAtMost(maxLevel)
    }

    fun visibleHints(hints: List<HintDefinition>, hintLevel: Int): List<HintDefinition> {
        return hints.filter { it.level <= hintLevel }.sortedBy { it.level }
    }
}
