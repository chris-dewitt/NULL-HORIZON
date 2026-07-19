package com.nullhorizon.app.content

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.app.content.model.ChapterDefinition
import org.junit.Test

class CampaignOrderTest {
    private fun chapter(id: String, vararg missions: String) =
        ChapterDefinition(
            schemaVersion = 1,
            chapterId = id,
            title = id,
            region = id,
            missionIds = missions.toList(),
        )

    private val chapters = listOf(
        // deliberately out of campaign order to prove sorting
        chapter("version_vault", "v.a", "v.b"),
        chapter("emergency_interface", "e.a", "e.b"),
        chapter("maintenance_deck", "m.a"),
        // vertical_slice re-lists real missions and must not create duplicates
        chapter("vertical_slice", "e.a", "m.a"),
        // an unknown chapter should be appended after the known campaign order
        chapter("bonus_wing", "b.a"),
    )

    @Test
    fun regionOrder_followsCampaignSequence_excludesVerticalSlice_appendsExtras() {
        val order = CampaignOrder.orderedRegionChapterIds(chapters.map { it.chapterId })
        assertThat(order).containsExactly(
            "emergency_interface",
            "maintenance_deck",
            "version_vault",
            "bonus_wing",
        ).inOrder()
        assertThat(order).doesNotContain("vertical_slice")
    }

    @Test
    fun missionSequence_isCampaignOrdered_andDeduplicated() {
        val ids = CampaignOrder.orderedMissionIds(chapters)
        assertThat(ids).containsExactly("e.a", "e.b", "m.a", "v.a", "v.b", "b.a").inOrder()
    }

    @Test
    fun nextMission_walksForwardAcrossChapters() {
        assertThat(CampaignOrder.nextMissionId(chapters, "e.a")).isEqualTo("e.b")
        // last mission of a chapter rolls into the next chapter
        assertThat(CampaignOrder.nextMissionId(chapters, "e.b")).isEqualTo("m.a")
        assertThat(CampaignOrder.nextMissionId(chapters, "m.a")).isEqualTo("v.a")
    }

    @Test
    fun nextMission_isNullForFinalOrUnknownMission() {
        assertThat(CampaignOrder.nextMissionId(chapters, "b.a")).isNull()
        assertThat(CampaignOrder.nextMissionId(chapters, "does.not.exist")).isNull()
    }
}
