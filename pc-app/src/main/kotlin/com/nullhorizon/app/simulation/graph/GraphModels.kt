package com.nullhorizon.app.simulation.graph

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Shared graph primitives for service-map and pipeline simulators.
 */
@Serializable
data class GraphNodeDefinition(
    val id: String,
    val kind: String,
    val label: String,
    val status: String = "unknown",
    val detail: String = "",
)

@Serializable
data class GraphEdgeDefinition(
    val id: String,
    @SerialName("from") val fromId: String,
    @SerialName("to") val toId: String,
    val kind: String = "data",
    val status: String = "ok",
    val detail: String = "",
)

@Serializable
data class GraphActionDefinition(
    val id: String,
    val label: String,
    val description: String? = null,
    val requires: Map<String, String> = emptyMap(),
    val effects: Map<String, String> = emptyMap(),
)

@Serializable
data class GraphNodeState(
    val id: String,
    val kind: String,
    val label: String,
    val status: String,
    val detail: String = "",
)

@Serializable
data class GraphEdgeState(
    val id: String,
    val fromId: String,
    val toId: String,
    val kind: String,
    val status: String,
    val detail: String = "",
)

object GraphStateOps {
    fun nodesFrom(defs: List<GraphNodeDefinition>): Map<String, GraphNodeState> =
        defs.associate { def ->
            def.id to GraphNodeState(
                id = def.id,
                kind = def.kind,
                label = def.label,
                status = def.status,
                detail = def.detail,
            )
        }

    fun edgesFrom(defs: List<GraphEdgeDefinition>): Map<String, GraphEdgeState> =
        defs.associate { def ->
            def.id to GraphEdgeState(
                id = def.id,
                fromId = def.fromId,
                toId = def.toId,
                kind = def.kind,
                status = def.status,
                detail = def.detail,
            )
        }

    fun unmetRequires(
        requires: Map<String, String>,
        lookup: (String) -> String?,
    ): Map<String, String> = requires.filter { (key, expected) -> lookup(key) != expected }

    fun applyEffects(
        effects: Map<String, String>,
        nodes: MutableMap<String, GraphNodeState>,
        edges: MutableMap<String, GraphEdgeState>,
        extras: MutableMap<String, String>,
    ): String? {
        var explanation: String? = null
        for ((key, value) in effects) {
            when {
                key == "explanation" -> explanation = value
                key.startsWith("node.") && key.endsWith(".status") -> {
                    val id = key.removePrefix("node.").removeSuffix(".status")
                    nodes[id] = nodes.getValue(id).copy(status = value)
                }
                key.startsWith("node.") && key.endsWith(".detail") -> {
                    val id = key.removePrefix("node.").removeSuffix(".detail")
                    nodes[id] = nodes.getValue(id).copy(detail = value)
                }
                key.startsWith("edge.") && key.endsWith(".status") -> {
                    val id = key.removePrefix("edge.").removeSuffix(".status")
                    edges[id] = edges.getValue(id).copy(status = value)
                }
                key.startsWith("edge.") && key.endsWith(".detail") -> {
                    val id = key.removePrefix("edge.").removeSuffix(".detail")
                    edges[id] = edges.getValue(id).copy(detail = value)
                }
                else -> extras[key] = value
            }
        }
        return explanation
    }
}
