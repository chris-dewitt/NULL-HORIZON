package com.nullhorizon.app.content

import com.nullhorizon.app.content.model.ChapterDefinition

/**
 * Canonical campaign ordering shared by the ship map and forward mission
 * navigation, so "next mission" and the region list agree on one sequence.
 *
 * The order is the spec §11 chapter progression; within a chapter it is the
 * chapter's declared mission order. The vertical-slice playlist is excluded
 * because it re-lists missions that already live in real chapters, so it must
 * not create duplicates or a second path.
 */
object CampaignOrder {
    val CHAPTER_ORDER: List<String> = listOf(
        "emergency_interface",
        "maintenance_deck",
        "version_vault",
        "archive_core",
        "automation_lab",
        "drone_foundry",
        "navigation_array",
        "communications_spire",
        "verification_chamber",
        "black_vault",
        "data_foundry",
        "reactor_kernel",
        "prediction_observatory",
        "horizon_core",
    )

    val NON_REGION_CHAPTERS: Set<String> = setOf("vertical_slice")

    /** Chapters to show as ship regions, campaign order first, extras appended. */
    fun orderedRegionChapterIds(available: List<String>): List<String> {
        val regions = available.filterNot { it in NON_REGION_CHAPTERS }
        val ordered = CHAPTER_ORDER.filter { it in regions }
        val remainder = regions.filterNot { it in CHAPTER_ORDER }
        return ordered + remainder
    }

    /**
     * Flat, de-duplicated mission sequence across the whole campaign, in the
     * order a player naturally progresses. Duplicate ids (a mission listed in
     * more than one chapter) keep only their first occurrence.
     */
    fun orderedMissionIds(chapters: List<ChapterDefinition>): List<String> {
        val byId = chapters.associateBy { it.chapterId }
        val seen = LinkedHashSet<String>()
        for (chapterId in orderedRegionChapterIds(chapters.map { it.chapterId })) {
            byId[chapterId]?.missionIds?.forEach { seen.add(it) }
        }
        return seen.toList()
    }

    /** The mission after [currentMissionId] in the campaign, or null if it is last/unknown. */
    fun nextMissionId(chapters: List<ChapterDefinition>, currentMissionId: String): String? {
        val ordered = orderedMissionIds(chapters)
        val index = ordered.indexOf(currentMissionId)
        return if (index >= 0 && index < ordered.lastIndex) ordered[index + 1] else null
    }
}
