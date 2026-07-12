package com.nullhorizon.app.simulation.mlops

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.feature.mission.engine.MissionPhase
import com.nullhorizon.app.feature.mission.engine.MissionStateMachine
import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Test

class MlOpsMissionTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun navRiskRollback_usesServiceMapAndMlOps() {
        val mission = loadMission("mlops.nav_risk_rollback.01")
        val machine = MissionStateMachine(mission)
        var state = machine.begin(machine.initialState())

        assertThat(state.serviceMap?.nodes?.get("monitoring")?.status).isEqualTo("alert")
        assertThat(state.mlops?.artifacts?.get("model_v7")?.stage).isEqualTo("production")

        state = machine.applyServiceMapAction(state, "acknowledge_alert")
        assertThat(state.serviceMap?.lastExplanation).contains("skew")
        assertThat(state.completedObjectiveIds).contains("ack_monitor")

        state = machine.applyMlOpsAction(state, "inspect_skew")
        assertThat(state.mlops?.lastExplanation).contains("bearing_rate")

        state = machine.applyMlOpsAction(state, "rollback_registry")
        assertThat(state.mlops?.artifacts?.get("model_v3")?.stage).isEqualTo("production")
        assertThat(state.mlops?.lastExplanation).contains("v3")

        state = machine.applyServiceMapAction(state, "route_stable")
        assertThat(state.serviceMap?.nodes?.get("inference")?.status).isEqualTo("healthy")
        assertThat(state.serviceMap?.nodes?.get("client")?.status).isEqualTo("healthy")
        assertThat(state.phase).isEqualTo(MissionPhase.Completed)
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
