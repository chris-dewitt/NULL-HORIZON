package com.nullhorizon.app.simulation.servicemap

import com.nullhorizon.app.simulation.graph.GraphActionDefinition
import com.nullhorizon.app.simulation.graph.GraphEdgeDefinition
import com.nullhorizon.app.simulation.graph.GraphEdgeState
import com.nullhorizon.app.simulation.graph.GraphNodeDefinition
import com.nullhorizon.app.simulation.graph.GraphNodeState
import com.nullhorizon.app.simulation.graph.GraphStateOps
import kotlinx.serialization.Serializable

@Serializable
data class ServiceMapDefinition(
    val nodes: List<GraphNodeDefinition>,
    val edges: List<GraphEdgeDefinition> = emptyList(),
    val actions: List<GraphActionDefinition> = emptyList(),
)

@Serializable
data class ServiceMapSessionState(
    val nodes: Map<String, GraphNodeState>,
    val edges: Map<String, GraphEdgeState>,
    val extras: Map<String, String> = emptyMap(),
    val lastActionId: String? = null,
    val lastExplanation: String? = null,
    val lastError: String? = null,
) {
    fun nodeStatus(id: String): String? = nodes[id]?.status

    fun edgeStatus(id: String): String? = edges[id]?.status

    fun snapshot(): Map<String, String> {
        val out = linkedMapOf<String, String>()
        nodes.values.forEach { node ->
            out["node.${node.id}.status"] = node.status
            out["node.${node.id}.kind"] = node.kind
            if (node.detail.isNotEmpty()) out["node.${node.id}.detail"] = node.detail
        }
        edges.values.forEach { edge ->
            out["edge.${edge.id}.status"] = edge.status
            out["edge.${edge.id}.kind"] = edge.kind
        }
        out.putAll(extras)
        lastExplanation?.let { out["last_explanation"] = it }
        return out
    }
}

object ServiceMapSimulator {
    fun fromDefinition(definition: ServiceMapDefinition): ServiceMapSessionState =
        ServiceMapSessionState(
            nodes = GraphStateOps.nodesFrom(definition.nodes),
            edges = GraphStateOps.edgesFrom(definition.edges),
        )

    fun applyAction(
        definition: ServiceMapDefinition,
        state: ServiceMapSessionState,
        actionId: String,
        context: Map<String, String> = emptyMap(),
    ): ServiceMapSessionState {
        val action = definition.actions.firstOrNull { it.id == actionId }
            ?: return state.copy(lastError = "Unknown service-map action.", lastExplanation = null)
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
        return state.copy(
            nodes = nodes,
            edges = edges,
            extras = extras,
            lastActionId = actionId,
            lastExplanation = explanation,
            lastError = null,
        )
    }
}
