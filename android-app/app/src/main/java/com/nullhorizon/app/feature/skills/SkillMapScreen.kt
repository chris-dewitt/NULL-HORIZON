package com.nullhorizon.app.feature.skills

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nullhorizon.app.R
import com.nullhorizon.app.progression.MasteryLevel

@Composable
fun SkillMapScreen(
    viewModel: SkillMapViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .semantics { contentDescription = "Skill map" },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.skills_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.skills_subtitle),
            style = MaterialTheme.typography.bodyLarge,
        )

        when {
            state.isLoading -> Text(stringResource(R.string.skills_loading))
            state.errorMessage != null -> Text(state.errorMessage.orEmpty())
            else -> {
                Text(
                    text = stringResource(
                        R.string.skills_rank_line,
                        state.rank,
                        state.clearancePoints,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (state.reviews.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.skills_review_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    state.reviews.forEach { rec ->
                        Text(
                            text = "${rec.skillId}: ${rec.reason}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Skill ${node.definition.name}"
            },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(node.definition.name, style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(
                R.string.skills_node_mastery,
                mastery.label(),
                node.mastery?.evidenceCount ?: 0,
                node.mastery?.unassistedEvidenceCount ?: 0,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        node.definition.description?.let {
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
        if (node.related.isNotEmpty()) {
            Text(
                text = stringResource(
                    R.string.skills_node_related,
                    node.related.joinToString(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
