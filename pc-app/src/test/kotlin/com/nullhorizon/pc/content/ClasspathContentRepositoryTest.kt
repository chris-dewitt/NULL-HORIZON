package com.nullhorizon.pc.content

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ClasspathContentRepositoryTest {
    @Test
    fun loadsManifestAndMissionFromResources() = runTest {
        val repository = ClasspathContentRepository()

        val manifest = repository.manifest()
        val mission = repository.mission("emergency.wake_sequence.01")

        assertThat(manifest.missions).contains("emergency.wake_sequence.01")
        assertThat(mission.missionId).isEqualTo("emergency.wake_sequence.01")
    }
}
