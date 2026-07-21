package com.nullhorizon.app.ui.chrome

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.nullhorizon.app.progression.AuditorFragment
import com.nullhorizon.app.ui.theme.NhColors

/**
 * Reveals the next Auditor mystery fragment after a repair — the slow-drip
 * narrative hook. Typewritten like an intercepted transmission.
 */
@Composable
fun AuditorPanel(
    fragment: AuditorFragment,
    modifier: Modifier = Modifier,
) {
    TuiPanel(
        title = "AUDITOR // SIGNAL ${fragment.index}/${fragment.total} DECODED",
        accent = NhColors.PhosphorBlue,
        modifier = modifier.semantics {
            contentDescription = "Auditor signal ${fragment.index} of ${fragment.total}"
        },
    ) {
        TypewriterText(
            text = fragment.text,
            style = MaterialTheme.typography.bodyMedium,
            color = NhColors.PhosphorWhite,
        )
    }
}
