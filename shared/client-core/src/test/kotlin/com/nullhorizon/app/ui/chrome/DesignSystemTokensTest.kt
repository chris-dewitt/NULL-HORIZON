package com.nullhorizon.app.ui.chrome

import com.nullhorizon.app.ui.theme.NhAccessibilityVisuals
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhPalette
import com.nullhorizon.app.ui.theme.NhRegionAccent
import com.nullhorizon.app.ui.theme.ShipRegionId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
        // Region accents are fixed semantic colours, independent of the active palette.
        assertEquals(NhPalette.GreenPhosphor.primary, accent.accent)
    }

    @Test
    fun regionAccent_blackVaultIsRed() {
        val accent = NhRegionAccent.forRegionId("black_vault")
        assertEquals(ShipRegionId.BlackVault, accent.region)
        assertEquals(NhPalette.GreenPhosphor.danger, accent.accent)
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

    @Test
    fun crtEffects_respectDisableCrtAndHighContrast() {
        val normal = NhAccessibilityVisuals()
        assertTrue(normal.crtEffectsEnabled)
        assertTrue(normal.animatedChromeEnabled)

        val disabled = NhAccessibilityVisuals(disableCrt = true)
        assertFalse(disabled.crtEffectsEnabled)
        assertTrue(disabled.animatedChromeEnabled)

        val highContrast = NhAccessibilityVisuals(highContrast = true)
        assertFalse(highContrast.crtEffectsEnabled)
        assertFalse(highContrast.animatedChromeEnabled)

        val reducedOnly = NhAccessibilityVisuals(reducedMotion = true)
        assertTrue(reducedOnly.crtEffectsEnabled)
        assertFalse(reducedOnly.animatedChromeEnabled)
    }

    @Test
    fun crtProfiles_mediumStrongerThanLean() {
        assertTrue(CrtProfile.Medium.scanlineAlpha > CrtProfile.Lean.scanlineAlpha)
        assertTrue(CrtProfile.Medium.vignetteStrength > CrtProfile.Lean.vignetteStrength)
    }
}
