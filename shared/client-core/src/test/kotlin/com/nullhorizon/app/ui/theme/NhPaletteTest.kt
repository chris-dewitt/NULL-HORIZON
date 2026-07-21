package com.nullhorizon.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test

class NhPaletteTest {
    @After
    fun restoreDefault() {
        NhColors.palette = NhPalette.GreenPhosphor
    }

    @Test
    fun defaultPalette_reproducesHistoricGreenPhosphorTokens() {
        NhColors.palette = NhPalette.GreenPhosphor
        assertThat(NhColors.CrtBlack).isEqualTo(Color(0xFF000703))
        assertThat(NhColors.CrtRaised).isEqualTo(Color(0xFF03180D))
        assertThat(NhColors.CrtPanel).isEqualTo(Color(0xFF011009))
        assertThat(NhColors.PhosphorWhite).isEqualTo(Color(0xFFDFFFE3))
        assertThat(NhColors.PhosphorDim).isEqualTo(Color(0xFF6FA67A))
        assertThat(NhColors.PhosphorGreen).isEqualTo(Color(0xFF35FF6B))
        assertThat(NhColors.PhosphorAmber).isEqualTo(Color(0xFFFFB000))
        assertThat(NhColors.PhosphorRed).isEqualTo(Color(0xFFFF3344))
        assertThat(NhColors.PhosphorBlue).isEqualTo(Color(0xFF44AAFF))
    }

    @Test
    fun switchingPalette_retintsTokens() {
        NhColors.palette = NhPalette.Amber
        assertThat(NhColors.PhosphorGreen).isEqualTo(NhPalette.Amber.primary)
        assertThat(NhColors.PhosphorAmber).isEqualTo(NhPalette.Amber.accent)
        assertThat(NhColors.CrtBlack).isEqualTo(NhPalette.Amber.ground)
    }

    @Test
    fun byId_resolvesKnownAndFallsBackToGreen() {
        assertThat(NhPalette.byId("ice")).isEqualTo(NhPalette.IceBlue)
        assertThat(NhPalette.byId("nope")).isEqualTo(NhPalette.GreenPhosphor)
    }

    @Test
    fun allPalettes_haveUniqueIdsAndDefaultUnlocksFree() {
        assertThat(NhPalette.all.map { it.id }).containsNoDuplicates()
        assertThat(NhPalette.GreenPhosphor.unlockClearance).isEqualTo(0)
    }
}
