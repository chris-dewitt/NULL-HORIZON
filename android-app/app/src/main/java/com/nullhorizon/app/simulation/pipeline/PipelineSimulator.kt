package com.nullhorizon.app.simulation.pipeline

import com.nullhorizon.app.simulation.graph.GraphActionDefinition
import com.nullhorizon.app.simulation.graph.GraphEdgeDefinition
import com.nullhorizon.app.simulation.graph.GraphEdgeState
import com.nullhorizon.app.simulation.graph.GraphNodeDefinition
import com.nullhorizon.app.simulation.graph.GraphNodeState
import com.nullhorizon.app.simulation.graph.GraphStateOps
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PipelineDefinition(
    @SerialName("run_id") val runId: String = "run-1",
    val nodes: List<GraphNodeDefinition>,
    val edges: List<GraphEdgeDefinition> = emptyList(),
    val actions: List<GraphActionDefinition> = emptyList(),
)

@Serializable
data class PipelineSessionState(
    val runId: String,
    val nodes: Map<String, GraphNodeState>,
    val edges: Map<String, GraphEdgeState>,
    val extras: Map<String, String> = emptyMap(),
    val lastActionId: String? = null,
    val lastExplanation: String? = null,
    val lastError: String? = null,
    val lastRunOutcome: String = "idle",
) {
    fun snapshot(): Map<String, String> {
        val out = linkedMapOf<String, String>()
        out["run_id"] = runId
        out["run_outcome"] = lastRunOutcome
        nodes.values.forEach { node ->
            out["node.${node.id}.status"] = node.status
            out["node.${node.id}.kind"] = node.kind
            if (node.detail.isNotEmpty()) out["node.${node.id}.detail"] = node.detail
        }
        edges.values.forEach { edge ->
            out["edge.${edge.id}.status"] = edge.status
        }
        out.putAll(extras)
        lastExplanation?.let { out["last_explanation"] = it }
        return out
    }
}

object PipelineSimulator {
    fun fromDefinition(definition: PipelineDefinition): PipelineSessionState =
        PipelineSessionState(
            runId = definition.runId,
            nodes = GraphStateOps.nodesFrom(definition.nodes),
            edges = GraphStateOps.edgesFrom(definition.edges),
            lastRunOutcome = "idle",
        )

    fun applyAction(
        definition: PipelineDefinition,
        state: PipelineSessionState,
        actionId: String,
        context: Map<String, String> = emptyMap(),
    ): PipelineSessionState {
        val action = definition.actions.firstOrNull { it.id == actionId }
            ?: return state.copy(lastError = "Unknown pipeline action.", lastExplanation = null)
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
        val nodes = state.nodes.toMutableMap()
        val edges = state.edges.toMutableMap()
        val extras = state.extras.toMutableMap()
        val explanation = GraphStateOps.applyEffects(action.effects, nodes, edges, extras)
            ?: action.description
            ?: "Applied ${action.label}"
        val outcome = extras.remove("run_outcome") ?: state.lastRunOutcome
        return state.copy(
            nodes = nodes,
            edges = edges,
            extras = extras,
            lastActionId = actionId,
            lastExplanation = explanation,
            lastError = null,
            lastRunOutcome = outcome,
        )
    }
}
