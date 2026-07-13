package com.nullhorizon.pc.feature.skills

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.progression.MasteryLevel
import com.nullhorizon.app.ui.chrome.TuiPanel
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme
import com.nullhorizon.pc.ui.Strings

@Composable
fun SkillMapScreen(
    viewModel: SkillMapViewModel,
) {
    val state by viewModel.uiState.collectAsState()
    val fontFamily = NhTheme.fontFamily

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .semantics { contentDescription = "Skill map" },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = Strings.skills_title,
            style = MaterialTheme.typography.headlineMedium,
            color = NhColors.PhosphorAmber,
            fontFamily = fontFamily,
        )
        Text(
            text = Strings.skills_subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = NhColors.PhosphorDim,
            fontFamily = fontFamily,
        )

        when {
            state.isLoading -> Text(Strings.skills_loading, color = NhColors.PhosphorDim)
            state.errorMessage != null -> Text(state.errorMessage.orEmpty(), color = NhColors.PhosphorRed)
            else -> {
                TuiPanel(title = "CLEARANCE", accent = NhColors.PhosphorGreen) {
                    Text(
                        text = Strings.skills_rank_line(state.rank, state.clearancePoints).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = NhColors.PhosphorWhite,
                        fontFamily = fontFamily,
                    )
                }
                if (state.reviews.isNotEmpty()) {
                    TuiPanel(title = Strings.skills_review_title, accent = NhColors.PhosphorAmber) {
                        state.reviews.forEach { rec ->
                            Text(
                                text = "${rec.skillId.uppercase()}: ${rec.reason}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NhColors.PhosphorDim,
                                fontFamily = fontFamily,
                            )
                        }
                    }
                }
                state.nodes.forEach { node ->
                    SkillNodeCard(node = node)
                }
            }
        }
    }
}

@Composable
private fun SkillNodeCard(node: SkillNodeUi) {
    val mastery = node.mastery?.masteryLevel ?: MasteryLevel.None
    val accent = when (mastery) {
        MasteryLevel.None -> NhColors.PhosphorDim
        MasteryLevel.Introduced -> NhColors.PhosphorBlue
        MasteryLevel.Practiced -> NhColors.PhosphorAmber
        MasteryLevel.Reliable -> NhColors.PhosphorGreen
        MasteryLevel.Mastered -> NhColors.PhosphorWhite
    }
    TuiPanel(
        title = node.definition.name,
        accent = accent,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Skill ${node.definition.name}" },
    ) {
        Text(
            text = Strings.skills_node_mastery(
                mastery.label(),
                node.mastery?.evidenceCount ?: 0,
                node.mastery?.unassistedEvidenceCount ?: 0,
            ).uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            color = accent,
            fontFamily = NhTheme.fontFamily,
        )
        node.definition.description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = NhColors.PhosphorWhite,
                fontFamily = NhTheme.fontFamily,
            )
        }
        if (node.related.isNotEmpty()) {
            Text(
                text = Strings.skills_node_related(node.related.joinToString()).uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = NhColors.PhosphorDim,
                fontFamily = NhTheme.fontFamily,
            )
        }
    }
}
