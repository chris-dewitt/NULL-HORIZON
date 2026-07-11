package com.nullhorizon.app.feature.mission.engine

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.pc.content.ClasspathContentRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MissionStateMachineSmokeTest {
    @Test
    fun initialStateLoadsFromClasspathMission() = runTest {
        val mission = ClasspathContentRepository().mission("emergency.fault_log.01")
        val state = MissionStateMachine(mission).initialState()

        assertThat(state.phase).isEqualTo(MissionPhase.Briefing)
        assertThat(state.completedObjectiveIds).isEmpty()
        assertThat(state.terminal).isNotNull()
    }
}
