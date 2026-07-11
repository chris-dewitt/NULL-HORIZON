package com.nullhorizon.app.data.mission

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlaceholderMissionsTest {
    @Test
    fun chapterZero_hasAvailableStarterAndLockedFollowUps() {
        val missions = PlaceholderMissions.chapterZero
        assertThat(missions).isNotEmpty()
        assertThat(missions.first().status).isEqualTo(MissionStatus.Available)
        assertThat(missions.drop(1).all { it.status == MissionStatus.Locked }).isTrue()
        assertThat(missions.map { it.id }.toSet()).hasSize(missions.size)
    }
}
