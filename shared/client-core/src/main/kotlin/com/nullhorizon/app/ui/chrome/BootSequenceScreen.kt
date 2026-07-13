package com.nullhorizon.app.ui.chrome

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme
import kotlinx.coroutines.delay

/**
 * Full-screen boot theatre. Instant-complete when reduced motion is enabled.
 * Click anywhere (or wait) to finish.
 */
@Composable
fun BootSequenceScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    lines: List<BootSequence.Line> = BootSequence.defaultLines(),
) {
    val reducedMotion = NhTheme.accessibility.reducedMotion
    var visibleCount by remember(reducedMotion) {
        mutableIntStateOf(if (reducedMotion) lines.size else 0)
    }

    LaunchedEffect(reducedMotion, lines) {
        if (reducedMotion) {
            visibleCount = lines.size
            delay(200)
            onFinished()
            return@LaunchedEffect
        }
        visibleCount = 0
        for (index in lines.indices) {
            delay(lines[index].delayMillis)
            visibleCount = index + 1
        }
        delay(500)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onFinished)
            .semantics { contentDescription = "Boot sequence. Activate to skip." }
            .padding(24.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            lines.take(visibleCount).forEach { line ->
                val isOk = line.text.contains("OK") || line.text.contains("ONLINE")
                Text(
                    text = line.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = when {
                        isOk -> NhColors.PhosphorGreen
                        line.text.contains("DEGRADED") -> NhColors.PhosphorAmber
                        else -> NhColors.PhosphorWhite
                    },
                    fontFamily = FontFamily.Monospace,
                )
            }
            if (visibleCount < lines.size && !reducedMotion) {
                BlockCursor(color = NhColors.PhosphorGreen)
            } else {
                Text(
                    text = "[ SKIP / CONTINUE ]",
                    style = MaterialTheme.typography.labelMedium,
                    color = NhColors.PhosphorDim,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}
