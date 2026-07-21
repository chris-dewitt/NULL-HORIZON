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

/**
 * The Signals log: every Auditor fragment the operator has decoded so far,
 * with still-encrypted entries shown as sealed until enough repairs are done.
 * Stateless — the hosting client loads [fragments] from content and computes
 * [decodedCount] from completed-mission progress.
 */
@Composable
fun SignalsLog(
    title: String,
    fragments: List<String>,
    decodedCount: Int,
    modifier: Modifier = Modifier,
) {
    val total = fragments.size
    val decoded = decodedCount.coerceIn(0, total)
    val fontFamily = NhTheme.fontFamily

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .semantics { contentDescription = "Signals log" },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "SIGNALS // $decoded/$total DECODED",
            style = MaterialTheme.typography.headlineMedium,
            color = NhColors.PhosphorAmber,
            fontFamily = fontFamily,
        )

        TuiPanel(title = title, accent = NhColors.PhosphorBlue) {
            Text(
                text = if (decoded == 0) {
                    "NO SIGNALS DECODED. COMPLETE A REPAIR TO INTERCEPT THE FIRST TRANSMISSION."
                } else {
                    "INTERCEPTED TRANSMISSIONS FROM AN UNAUTHORISED AUDIT PROCESS. " +
                        "EACH REPAIR DECODES THE NEXT."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = NhColors.PhosphorDim,
                fontFamily = fontFamily,
            )
        }

        if (total == 0) return@Column

        fragments.forEachIndexed { index, text ->
            val number = index + 1
            val isDecoded = index < decoded
            if (isDecoded) {
                TuiPanel(
                    title = "SIGNAL $number/$total",
                    accent = NhColors.PhosphorGreen,
                    modifier = Modifier.semantics {
                        contentDescription = "Decoded signal $number of $total"
                    },
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NhColors.PhosphorWhite,
                        fontFamily = fontFamily,
                    )
                }
            } else {
                TuiPanel(
                    title = "SIGNAL $number/$total",
                    accent = NhColors.PhosphorDim,
                    modifier = Modifier.semantics {
                        contentDescription = "Encrypted signal $number of $total, decodes after $number repairs"
                    },
                ) {
                    Text(
                        text = "▓▓▓▓▓▓  ENCRYPTED  ▓▓▓▓▓▓  DECODES AFTER $number REPAIRS",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NhColors.PhosphorDim,
                        fontFamily = fontFamily,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
