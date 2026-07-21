package com.nullhorizon.app.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhPalette
import com.nullhorizon.app.ui.theme.NhTheme

/**
 * Terminal-palette selector. Each row previews a palette with its own colours
 * (so you see the phosphor before choosing) and re-tints the whole app on tap.
 */
@Composable
fun PalettePicker(
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    clearance: Int = Int.MAX_VALUE,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NhPalette.all.forEach { palette ->
            val selected = palette.id == selectedId
            val locked = palette.unlockClearance > clearance
            val edge = when {
                selected -> palette.accent
                locked -> NhColors.PhosphorDim.copy(alpha = 0.4f)
                else -> NhColors.PhosphorDim
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.ground.copy(alpha = if (locked) 0.5f else 1f))
                    .drawTuiBorder(color = edge)
                    .clickable(enabled = !locked) { onSelect(palette.id) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .semantics {
                        this.contentDescription = if (locked) {
                            "${palette.displayName} palette, locked, unlocks at " +
                                "${palette.unlockClearance} clearance"
                        } else {
                            "${palette.displayName} palette${if (selected) ", selected" else ""}"
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val fade = if (locked) 0.5f else 1f
                Text(
                    text = when {
                        locked -> "[-]"
                        selected -> "[x]"
                        else -> "[ ]"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.accent.copy(alpha = fade),
                    fontFamily = NhTheme.fontFamily,
                )
                Swatch(palette.primary.copy(alpha = fade))
                Swatch(palette.accent.copy(alpha = fade))
                Swatch(palette.text.copy(alpha = fade))
                Text(
                    text = if (locked) {
                        "${palette.displayName.uppercase()} — UNLOCKS AT ${palette.unlockClearance}"
                    } else {
                        palette.displayName.uppercase()
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.text.copy(alpha = fade),
                    fontFamily = NhTheme.fontFamily,
                )
            }
        }
    }
}

@Composable
private fun Swatch(color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .size(14.dp)
            .background(color)
            .drawTuiBorder(color = NhColors.PhosphorDim),
    ) {}
}
