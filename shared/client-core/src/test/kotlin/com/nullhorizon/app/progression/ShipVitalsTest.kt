package com.nullhorizon.app.progression

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ShipVitalsTest {
    @Test
    fun vitals_areZeroBeforeAnyRepair() {
        val vitals = ShipVitals.from(
            regions = listOf(RegionProgress(0, 6), RegionProgress(0, 4)),
            signalsDecoded = 0,
            signalsTotal = 9,
        )
        assertThat(vitals.hullPercent).isEqualTo(0)
        assertThat(vitals.powerPercent).isEqualTo(0)
        assertThat(vitals.dataPercent).isEqualTo(0)
        assertThat(vitals.regionsTotal).isEqualTo(2)
    }

    @Test
    fun powerCountsOnlyFullyRestoredRegions() {
        val vitals = ShipVitals.from(
            regions = listOf(RegionProgress(6, 6), RegionProgress(3, 4)),
            signalsDecoded = 5,
            signalsTotal = 9,
        )
        assertThat(vitals.missionsCompleted).isEqualTo(9)
        assertThat(vitals.missionsTotal).isEqualTo(10)
        assertThat(vitals.hullPercent).isEqualTo(90)
        assertThat(vitals.regionsRestored).isEqualTo(1)
        assertThat(vitals.powerPercent).isEqualTo(50)
        assertThat(vitals.dataPercent).isEqualTo(55)
    }

    @Test
    fun emptyRegionsDoNotCountAgainstTheShip() {
        val vitals = ShipVitals.from(
            regions = listOf(RegionProgress(2, 2), RegionProgress(0, 0)),
            signalsDecoded = 0,
            signalsTotal = 0,
        )
        assertThat(vitals.regionsTotal).isEqualTo(1)
        assertThat(vitals.hullPercent).isEqualTo(100)
        assertThat(vitals.powerPercent).isEqualTo(100)
        assertThat(vitals.dataPercent).isEqualTo(0)
    }

    @Test
    fun bandsSplitCriticalDegradedNominal() {
        assertThat(VitalsBand.forPercent(0)).isEqualTo(VitalsBand.Critical)
        assertThat(VitalsBand.forPercent(33)).isEqualTo(VitalsBand.Critical)
        assertThat(VitalsBand.forPercent(34)).isEqualTo(VitalsBand.Degraded)
        assertThat(VitalsBand.forPercent(66)).isEqualTo(VitalsBand.Degraded)
        assertThat(VitalsBand.forPercent(67)).isEqualTo(VitalsBand.Nominal)
        assertThat(VitalsBand.forPercent(100)).isEqualTo(VitalsBand.Nominal)
    }
}
