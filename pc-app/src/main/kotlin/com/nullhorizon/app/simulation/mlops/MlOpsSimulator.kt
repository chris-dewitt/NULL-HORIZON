package com.nullhorizon.app.simulation.mlops

import com.nullhorizon.app.simulation.graph.GraphActionDefinition
import com.nullhorizon.app.simulation.graph.GraphStateOps
import kotlinx.serialization.Serializable

@Serializable
data class MlArtifactDefinition(
    val id: String,
    val kind: String,
    val label: String,
    val stage: String = "none",
    val status: String = "unknown",
    val version: String = "",
    val detail: String = "",
)

@Serializable
data class MlOpsDefinition(
    val artifacts: List<MlArtifactDefinition>,
    val actions: List<GraphActionDefinition> = emptyList(),
)

@Serializable
data class MlArtifactState(
    val id: String,
    val kind: String,
    val label: String,
    val stage: String,
    val status: String,
    val version: String = "",
    val detail: String = "",
)

@Serializable
data class MlOpsSessionState(
    val artifacts: Map<String, MlArtifactState>,
    val extras: Map<String, String> = emptyMap(),
    val lastActionId: String? = null,
    val lastExplanation: String? = null,
    val lastError: String? = null,
) {
    fun snapshot(): Map<String, String> {
        val out = linkedMapOf<String, String>()
        artifacts.values.forEach { art ->
            out["artifact.${art.id}.stage"] = art.stage
            out["artifact.${art.id}.status"] = art.status
            out["artifact.${art.id}.kind"] = art.kind
            if (art.version.isNotEmpty()) out["artifact.${art.id}.version"] = art.version
            if (art.detail.isNotEmpty()) out["artifact.${art.id}.detail"] = art.detail
        }
        out.putAll(extras)
        lastExplanation?.let { out["last_explanation"] = it }
        return out
    }
}

object MlOpsSimulator {
    fun fromDefinition(definition: MlOpsDefinition): MlOpsSessionState =
        MlOpsSessionState(
            artifacts = definition.artifacts.associate { def ->
                def.id to MlArtifactState(
                    id = def.id,
                    kind = def.kind,
                    label = def.label,
                    stage = def.stage,
                    status = def.status,
                    version = def.version,
                    detail = def.detail,
                )
            },
        )

    fun applyAction(
        definition: MlOpsDefinition,
        state: MlOpsSessionState,
        actionId: String,
        context: Map<String, String> = emptyMap(),
    ): MlOpsSessionState {
        val action = definition.actions.firstOrNull { it.id == actionId }
            ?: return state.copy(lastError = "Unknown ML-ops action.", lastExplanation = null)
        val snapshot = context + state.snapshot()
        val unmet = GraphStateOps.unmetRequires(action.requires) { key ->
            snapshot[key]
        }
        if (unmet.isNotEmpty()) {
            return state.copy(
                lastError = "Action blocked. Requirements not met: ${unmet.keys.joinToString()}",
                lastExplanation = null,
                lastActionId = actionId,
            )
        }
        val artifacts = state.artifacts.toMutableMap()
        val extras = state.extras.toMutableMap()
        var explanation: String? = null
        for ((key, value) in action.effects) {
            when {
                key == "explanation" -> explanation = value
                key.startsWith("artifact.") && key.endsWith(".stage") -> {
                    val id = key.removePrefix("artifact.").removeSuffix(".stage")
                    artifacts[id] = artifacts.getValue(id).copy(stage = value)
                }
                key.startsWith("artifact.") && key.endsWith(".status") -> {
                    val id = key.removePrefix("artifact.").removeSuffix(".status")
                    artifacts[id] = artifacts.getValue(id).copy(status = value)
                }
                key.startsWith("artifact.") && key.endsWith(".version") -> {
                    val id = key.removePrefix("artifact.").removeSuffix(".version")
                    artifacts[id] = artifacts.getValue(id).copy(version = value)
                }
                key.startsWith("artifact.") && key.endsWith(".detail") -> {
                    val id = key.removePrefix("artifact.").removeSuffix(".detail")
                    artifacts[id] = artifacts.getValue(id).copy(detail = value)
                }
                else -> extras[key] = value
            }
        }
        return state.copy(
            artifacts = artifacts,
            extras = extras,
            lastActionId = actionId,
            lastExplanation = explanation ?: action.description ?: "Applied ${action.label}",
            lastError = null,
        )
    }
}
