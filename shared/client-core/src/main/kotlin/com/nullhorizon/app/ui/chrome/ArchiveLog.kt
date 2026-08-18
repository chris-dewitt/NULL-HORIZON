package com.nullhorizon.app.ui.chrome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme

/** One recoverable archive record: lore, a schematic, a badge. */
data class ArchiveEntry(
    val rewardId: String,
    val name: String,
    val kind: String,
    val description: String?,
    val unlocked: Boolean,
)

/**
 * The Archive: every record the operator has recovered from the ship, plus the
 * sealed ones still waiting behind a repair. Mission rewards already unlock
 * these records; this is where they can actually be read.
 */
@Composable
fun ArchiveLog(
    entries: List<ArchiveEntry>,
    modifier: Modifier = Modifier,
) {
    val fontFamily = NhTheme.fontFamily
    val recovered = entries.count { it.unlocked }
    val total = entries.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .semantics { contentDescription = "Archive, $recovered of $total records recovered" },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "ARCHIVE // $recovered/$total RECOVERED",
            style = MaterialTheme.typography.headlineMedium,
            color = NhColors.PhosphorAmber,
            fontFamily = fontFamily,
        )
        Text(
            text = meterBar(recovered, total, cells = 16),
            style = MaterialTheme.typography.bodyMedium,
            color = if (recovered == 0) NhColors.PhosphorDim else NhColors.PhosphorGreen,
            fontFamily = fontFamily,
        )

        if (total == 0) {
            Text(
                text = "NO RECORDS INDEXED.",
                style = MaterialTheme.typography.bodyMedium,
                color = NhColors.PhosphorDim,
                fontFamily = fontFamily,
            )
            return@Column
        }

        Text(
            text = if (recovered == 0) {
                "SHIP RECORDS ARE FRAGMENTED. EVERY COMPLETED REPAIR RECOVERS ONE."
            } else {
                "RECOVERED RECORDS ARE READABLE BELOW. SEALED RECORDS NEED THE REPAIR THAT HOLDS THEM."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = NhColors.PhosphorDim,
            fontFamily = fontFamily,
        )

        entries.forEach { entry ->
            if (entry.unlocked) {
                TuiPanel(
                    title = entry.name,
                    accent = NhColors.PhosphorGreen,
                    modifier = Modifier.semantics {
                        contentDescription = "Recovered record ${entry.name}"
                    },
                ) {
                    Text(
                        text = entry.kind.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = NhColors.PhosphorDim,
                        fontFamily = fontFamily,
                    )
                    Text(
                        text = entry.description.orEmpty().ifBlank { "No further detail recovered." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = NhColors.PhosphorWhite,
                        fontFamily = fontFamily,
                    )
                }
            } else {
                TuiPanel(
                    title = "SEALED RECORD",
                    accent = NhColors.PhosphorDim,
                    modifier = Modifier.semantics {
                        contentDescription = "Sealed record, recovered by completing its repair"
                    },
                ) {
                    Text(
                        text = redacted(entry.name),
                        style = MaterialTheme.typography.bodyMedium,
                        color = NhColors.PhosphorDim,
                        fontFamily = fontFamily,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "SEALED — COMPLETE THE REPAIR THAT HOLDS THIS RECORD",
                        style = MaterialTheme.typography.labelMedium,
                        color = NhColors.PhosphorDim,
                        fontFamily = fontFamily,
                    )
                }
            }
        }
    }
}

/**
 * Redacts a record title while keeping its shape, so a sealed entry still
 * reads as a specific missing thing rather than a blank row.
 */
internal fun redacted(name: String): String =
    name.map { char -> if (char.isWhitespace()) ' ' else '▓' }.joinToString("")
