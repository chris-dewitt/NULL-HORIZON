package com.nullhorizon.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * A single-phosphor terminal palette. Each preset re-tints the whole chrome
 * (grounds, text, primary/accent phosphor) to one monitor family, while
 * keeping [danger] a distinct warning colour so errors stay legible. Unlocked
 * by clearance so alternate phosphors are a progression reward (design pick 4A).
 */
data class NhPalette(
    val id: String,
    val displayName: String,
    val unlockClearance: Int,
    val ground: Color,
    val raised: Color,
    val panel: Color,
    val text: Color,
    val dim: Color,
    val primary: Color,
    val accent: Color,
    val accentDim: Color,
    val info: Color,
    val danger: Color,
) {
    companion object {
        /** Default; exactly the historic green-phosphor tokens (ADR-0022). */
        val GreenPhosphor = NhPalette(
            id = "green",
            displayName = "Green Phosphor",
            unlockClearance = 0,
            ground = Color(0xFF000703),
            raised = Color(0xFF03180D),
            panel = Color(0xFF011009),
            text = Color(0xFFDFFFE3),
            dim = Color(0xFF6FA67A),
            primary = Color(0xFF35FF6B),
            accent = Color(0xFFFFB000),
            accentDim = Color(0xFFB07A00),
            info = Color(0xFF44AAFF),
            danger = Color(0xFFFF3344),
        )

        val Amber = NhPalette(
            id = "amber",
            displayName = "Amber CRT",
            unlockClearance = 60,
            ground = Color(0xFF0A0600),
            raised = Color(0xFF1A1200),
            panel = Color(0xFF120C00),
            text = Color(0xFFFFE8B0),
            dim = Color(0xFFB08A50),
            primary = Color(0xFFFFB000),
            accent = Color(0xFFFFD000),
            accentDim = Color(0xFFB07A00),
            info = Color(0xFFFFCC66),
            danger = Color(0xFFFF5533),
        )

        val IceBlue = NhPalette(
            id = "ice",
            displayName = "Ice Blue",
            unlockClearance = 180,
            ground = Color(0xFF00060B),
            raised = Color(0xFF031420),
            panel = Color(0xFF010E18),
            text = Color(0xFFDDF2FF),
            dim = Color(0xFF6F98B0),
            primary = Color(0xFF44CCFF),
            accent = Color(0xFF88E0FF),
            accentDim = Color(0xFF3F7A99),
            info = Color(0xFF66DDFF),
            danger = Color(0xFFFF4466),
        )

        val Monochrome = NhPalette(
            id = "mono",
            displayName = "Monochrome",
            unlockClearance = 360,
            ground = Color(0xFF050505),
            raised = Color(0xFF161616),
            panel = Color(0xFF101010),
            text = Color(0xFFF0F0F0),
            dim = Color(0xFF8A8A8A),
            primary = Color(0xFFD8D8D8),
            accent = Color(0xFFFFFFFF),
            accentDim = Color(0xFF9A9A9A),
            info = Color(0xFFBFBFBF),
            danger = Color(0xFFFF6666),
        )

        /**
         * Applied automatically when high-contrast is on (not user-selectable),
         * so the whole NhColors-based UI honours the accessibility setting.
         */
        val HighContrast = NhPalette(
            id = "high_contrast",
            displayName = "High Contrast",
            unlockClearance = 0,
            ground = Color(0xFF000000),
            raised = Color(0xFF101010),
            panel = Color(0xFF0A0A0A),
            text = Color(0xFFFFFFF0),
            dim = Color(0xFFCFCFCF),
            primary = Color(0xFFFFFFFF),
            accent = Color(0xFFFFEE55),
            accentDim = Color(0xFFBFBF44),
            info = Color(0xFF99CCFF),
            danger = Color(0xFFFF6666),
        )

        /** User-selectable palettes, in unlock order (excludes high-contrast). */
        val all: List<NhPalette> = listOf(GreenPhosphor, Amber, IceBlue, Monochrome)

        fun byId(id: String): NhPalette = all.firstOrNull { it.id == id } ?: GreenPhosphor
    }
}
