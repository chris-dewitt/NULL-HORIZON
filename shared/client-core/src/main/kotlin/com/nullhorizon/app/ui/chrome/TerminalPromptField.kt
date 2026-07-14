package com.nullhorizon.app.ui.chrome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme

/**
 * Terminal-style command line: prompt + field + blinking block cursor + run.
 */
@Composable
fun TerminalPromptField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    prompt: String = "> ",
    enabled: Boolean = true,
    contentDescription: String = "Terminal input",
    runLabel: String = "RUN",
) {
    val fontFamily = NhTheme.fontFamily
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawTuiBorder(color = NhColors.PhosphorGreen)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = prompt,
            color = NhColors.PhosphorGreen,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = fontFamily,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            cursorBrush = SolidColor(NhColors.PhosphorGreen),
            textStyle = TextStyle(
                color = NhColors.PhosphorWhite,
                fontFamily = fontFamily,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            ),
            modifier = Modifier
                .weight(1f)
                .semantics { this.contentDescription = contentDescription },
            decorationBox = { inner ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    inner()
                    if (enabled && value.isEmpty()) {
                        BlockCursor(color = NhColors.PhosphorGreen)
                    }
                }
            },
        )
        TuiActionButton(
            label = runLabel,
            onClick = onSubmit,
            enabled = enabled && value.isNotBlank(),
            accent = NhColors.PhosphorAmber,
            contentDescription = "Run command",
        )
    }
}

/**
 * ORION/MICA (or other) dialogue with typewriter reveal. Speaker labels are
 * chrome (uppercase); spoken text stays sentence case.
 */
@Composable
fun DialogueLines(
    lines: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    accentSpeaker: String? = null,
) {
    val fontFamily = NhTheme.fontFamily
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        lines.forEach { (speaker, text) ->
            val speakerColor = when {
                accentSpeaker != null && speaker.equals(accentSpeaker, ignoreCase = true) ->
                    NhColors.PhosphorAmber
                speaker.equals("ORION", ignoreCase = true) -> NhColors.PhosphorAmber
                speaker.equals("MICA", ignoreCase = true) -> NhColors.PhosphorBlue
                else -> NhColors.PhosphorGreen
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = speaker.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = speakerColor,
                    fontFamily = fontFamily,
                )
                TypewriterText(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NhColors.PhosphorWhite,
                )
            }
        }
    }
}
