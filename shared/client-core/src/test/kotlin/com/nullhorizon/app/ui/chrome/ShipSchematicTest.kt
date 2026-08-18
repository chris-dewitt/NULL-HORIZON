package com.nullhorizon.app.ui.chrome

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ShipSchematicTest {
    @Test
    fun meterBar_fillsProportionallyAndKeepsWidth() {
        assertThat(meterBar(0, 6)).isEqualTo("▱▱▱▱▱▱▱▱")
        assertThat(meterBar(3, 6)).isEqualTo("▰▰▰▰▱▱▱▱")
        assertThat(meterBar(6, 6)).isEqualTo("▰▰▰▰▰▰▰▰")
        assertThat(meterBar(1, 6).length).isEqualTo(8)
    }

    @Test
    fun meterBar_handlesEmptyAndOverfullRegions() {
        assertThat(meterBar(0, 0)).isEqualTo("▱▱▱▱▱▱▱▱")
        assertThat(meterBar(9, 6)).isEqualTo("▰▰▰▰▰▰▰▰")
    }

    @Test
    fun redacted_keepsWordShapeWithoutLeakingTheTitle() {
        assertThat(redacted("Missing Crew Ledger")).isEqualTo("▓▓▓▓▓▓▓ ▓▓▓▓ ▓▓▓▓▓▓")
        assertThat(redacted("")).isEmpty()
    }
}
