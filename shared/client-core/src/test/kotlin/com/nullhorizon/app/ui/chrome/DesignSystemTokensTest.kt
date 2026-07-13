package com.nullhorizon.app.ui.chrome

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhRegionAccent
import com.nullhorizon.app.ui.theme.ShipRegionId

class DesignSystemTokensTest {
    @Test
    fun bootSequence_startsWithOsVersion() {
        val lines = BootSequence.defaultLines()
        assertEquals(BootSequence.OS_VERSION, lines.first().text)
        assertTrue(lines.any { it.text.contains("MEMORY CHECK") })
        assertTrue(lines.any { it.text.contains("OK") })
    }

    @Test
    fun regionAccent_archiveIsGreen() {
        val accent = NhRegionAccent.forRegionId("archive")
        assertEquals(ShipRegionId.Archive, accent.region)
        assertEquals(NhColors.PhosphorGreen, accent.accent)
    }

    @Test
    fun regionAccent_blackVaultIsRed() {
        val accent = NhRegionAccent.forRegionId("black_vault")
        assertEquals(ShipRegionId.BlackVault, accent.region)
        assertEquals(NhColors.PhosphorRed, accent.accent)
    }

    @Test
    fun regionAccent_resolvesDisplayNames() {
        assertEquals(ShipRegionId.Emergency, ShipRegionId.fromRawId("emergency_interface"))
        assertEquals(ShipRegionId.VersionVault, ShipRegionId.fromRawId("version_vault"))
    }

    @Test
    fun statusLine_isAllCapsPattern() {
        val line = NhRegionAccent.statusLine("Archive Core", "Degraded")
        assertEquals("REGION: ARCHIVE CORE — DEGRADED", line)
        assertFalse(line.any { it.isLowerCase() })
    }
}
