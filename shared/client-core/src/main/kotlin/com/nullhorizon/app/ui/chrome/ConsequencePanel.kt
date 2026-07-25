package com.nullhorizon.app.ui.chrome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.content.model.ConsequenceDefinition
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme

/**
 * Concrete fallout when a repair backfires — §9.9 "failure feedback should be
 * equally concrete." Surfaced in-world after a setback (e.g. a failing test
 * run) so a botched attempt has visible weight, then clears once recovered.
 */
@Composable
fun ConsequencePanel(
    consequence: ConsequenceDefinition,
    modifier: Modifier = Modifier,
) {
    val fontFamily = NhTheme.fontFamily
    TuiPanel(
        title = "CONSEQUENCE // ${consequence.title.uppercase()}",
        accent = NhColors.PhosphorRed,
        modifier = modifier.semantics {
            contentDescription = "Consequence: ${consequence.title}. ${consequence.symptom}"
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = consequence.symptom,
                style = MaterialTheme.typography.bodyMedium,
                color = NhColors.PhosphorRed,
                fontFamily = fontFamily,
            )
            Text(
                text = consequence.fallout,
                style = MaterialTheme.typography.bodySmall,
                color = NhColors.PhosphorDim,
                fontFamily = fontFamily,
            )
        }
    }
}
