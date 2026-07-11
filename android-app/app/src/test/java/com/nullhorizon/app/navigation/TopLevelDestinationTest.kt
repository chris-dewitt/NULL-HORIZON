package com.nullhorizon.app.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TopLevelDestinationTest {
    @Test
    fun primaryNavigation_hasAccessibilityLabelsForAllTabs() {
        TopLevelDestination.entries.forEach { destination ->
            assertThat(destination.contentDescriptionRes).isNotEqualTo(0)
            assertThat(destination.labelRes).isNotEqualTo(0)
            assertThat(destination.route).isNotEmpty()
        }
        assertThat(TopLevelDestination.entries.map { it.route })
            .containsExactly("ship_map", "missions", "settings")
            .inOrder()
    }
}
