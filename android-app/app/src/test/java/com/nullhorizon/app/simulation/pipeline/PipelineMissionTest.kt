package com.nullhorizon.app.simulation.pipeline

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.feature.mission.engine.MissionPhase
import com.nullhorizon.app.feature.mission.engine.MissionStateMachine
import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Test

class PipelineMissionTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun telemetryDropMission_isDeterministicAndExplainable() {
        val mission = loadMission("pipeline.telemetry_drop.01")
        val machine = MissionStateMachine(mission)
        var state = machine.begin(machine.initialState())

        state = machine.applyPipelineAction(state, "inspect_validate")
        assertThat(state.pipeline?.lastExplanation).contains("soft drops")
        assertThat(state.completedObjectiveIds).contains("inspect_drop")

        state = machine.applyPipelineAction(state, "rerun_pipeline")
        assertThat(state.pipeline?.lastError).contains("Requirements not met")

        state = machine.applyPipelineAction(state, "fix_quality_gate")
        assertThat(state.pipeline?.nodes?.get("validate")?.status).isEqualTo("succeeded")
        assertThat(state.pipeline?.lastExplanation).contains("Quality gate")

        state = machine.applyPipelineAction(state, "rerun_pipeline")
        assertThat(state.pipeline?.lastRunOutcome).isEqualTo("succeeded")
        assertThat(state.pipeline?.lastExplanation).contains("Full DAG completed")
        assertThat(state.phase).isEqualTo(MissionPhase.Completed)

        val reset = machine.reset()
        assertThat(reset.pipeline?.nodes?.get("validate")?.status).isEqualTo("failed")
        assertThat(reset.pipeline?.lastRunOutcome).isEqualTo("idle")
    }

    private fun loadMission(id: String): MissionDefinition {
        val candidates = listOf(
            File("src/main/assets/content/missions/$id.json"),
            File("app/src/main/assets/content/missions/$id.json"),
        )
        val file = candidates.first { it.exists() }
        return json.decodeFromString(file.readText())
    }
}
