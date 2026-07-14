package com.nullhorizon.app.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme

/**
 * Terminal-style action control: box border + ALL-CAPS label (no Material card).
 */
@Composable
fun TuiActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = NhColors.PhosphorAmber,
    contentDescription: String = label,
    inverted: Boolean = false,
) {
    val color = when {
        !enabled -> NhColors.PhosphorDim
        inverted -> NhColors.CrtBlack
        else -> accent
    }
    Box(
        modifier = modifier
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            }
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .then(
                if (enabled && inverted) {
                    Modifier.background(accent)
                } else {
                    Modifier
                },
            )
            .drawTuiBorder(color = color)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.trim().uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontFamily = NhTheme.fontFamily,
        )
    }
}

data class TuiNavItem(
    val id: String,
    val label: String,
    val contentDescription: String,
    val keyHint: String? = null,
)

/**
 * Pure TUI side column (tmux/Palantir pane) for PC primary navigation.
 */
@Composable
fun TuiNavColumn(
    title: String,
    subtitle: String?,
    items: List<TuiNavItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fontFamily = NhTheme.fontFamily
    Column(
        modifier = modifier
            .fillMaxHeight()
            .widthIn(min = 132.dp, max = 168.dp)
            .drawTuiBorder(color = NhColors.PhosphorDim)
            .padding(8.dp)
            .semantics { contentDescription = "Primary navigation" },
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = NhColors.PhosphorAmber,
            fontFamily = fontFamily,
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = NhColors.PhosphorDim,
                fontFamily = fontFamily,
            )
        }
        items.forEachIndexed { index, item ->
            val selected = item.id == selectedId
            val accent = if (selected) NhColors.PhosphorAmber else NhColors.PhosphorDim
            val hint = item.keyHint ?: (index + 1).toString()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = item.contentDescription
                        this.selected = selected
                        role = Role.Tab
                    }
                    .clickable { onSelect(item.id) }
                    .then(
                        if (selected) {
                            Modifier.background(NhColors.PhosphorAmber)
                        } else {
                            Modifier
                        },
                    )
                    .drawTuiBorder(color = accent)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) NhColors.CrtBlack else accent,
                    fontFamily = fontFamily,
                    modifier = Modifier.width(16.dp),
                )
                Text(
                    text = item.label.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) NhColors.CrtBlack else NhColors.PhosphorDim,
                    fontFamily = fontFamily,
                )
            }
        }
    }
}

/**
 * tmux-style horizontal tab line: session tag, indexed tabs, right status
 * segment. The selected tab inverts to the accent, matching a tmux
 * window-status-current. Android primary navigation; also fits any surface
 * that needs a horizontal pane switcher.
 */
@Composable
fun TuiTabLine(
    sessionTag: String,
    items: List<TuiNavItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    statusText: String? = null,
) {
    val fontFamily = NhTheme.fontFamily
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawTuiBorder(color = NhColors.PhosphorDim)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics { contentDescription = "Primary navigation" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = sessionTag,
            style = MaterialTheme.typography.labelMedium,
            color = NhColors.PhosphorGreen,
            fontFamily = fontFamily,
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items.forEachIndexed { index, item ->
                val selected = item.id == selectedId
                val hint = item.keyHint ?: index.toString()
                Text(
                    text = "$hint:${item.label.trim().uppercase()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) NhColors.CrtBlack else NhColors.PhosphorDim,
                    fontFamily = fontFamily,
                    modifier = Modifier
                        .semantics {
                            contentDescription = item.contentDescription
                            this.selected = selected
                            role = Role.Tab
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelect(item.id) }
                        .then(
                            if (selected) {
                                Modifier.background(NhColors.PhosphorAmber)
                            } else {
                                Modifier
                            },
                        )
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                )
            }
        }
        statusText?.let {
            Text(
                text = "● ${it.trim().uppercase()}",
                style = MaterialTheme.typography.labelMedium,
                color = NhColors.PhosphorGreen,
                fontFamily = fontFamily,
            )
        }
    }
}
