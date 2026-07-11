package com.nullhorizon.app.simulation.graph

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.app.simulation.pipeline.PipelineDefinition
import com.nullhorizon.app.simulation.pipeline.PipelineSimulator
import org.junit.Test

class GraphSimulatorSeedTest {
    @Test
    fun pipelineSeed_isDeterministicAcrossResets() {
        val definition = PipelineDefinition(
            runId = "r1",
            nodes = listOf(
                GraphNodeDefinition(
                    id = "validate",
                    kind = "validate",
                    label = "Validate",
                    status = "failed",
                    detail = "dropping",
                ),
            ),
            actions = listOf(
                GraphActionDefinition(
                    id = "fix",
                    label = "Fix",
                    effects = mapOf(
                        "node.validate.status" to "succeeded",
                        "explanation" to "fixed",
                    ),
                ),
            ),
        )
        val first = PipelineSimulator.fromDefinition(definition)
        val second = PipelineSimulator.fromDefinition(definition)
        assertThat(first).isEqualTo(second)

        val mutated = PipelineSimulator.applyAction(definition, first, "fix")
        assertThat(mutated.nodes.getValue("validate").status).isEqualTo("succeeded")
        assertThat(mutated.lastExplanation).isEqualTo("fixed")

        val reset = PipelineSimulator.fromDefinition(definition)
        assertThat(reset.nodes.getValue("validate").status).isEqualTo("failed")
    }
}
