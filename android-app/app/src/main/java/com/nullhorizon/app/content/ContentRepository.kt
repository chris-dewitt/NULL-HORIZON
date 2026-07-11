package com.nullhorizon.app.content

import com.nullhorizon.app.content.model.ChapterDefinition
import com.nullhorizon.app.content.model.ContentManifest
import com.nullhorizon.app.content.model.DialogueDefinition
import com.nullhorizon.app.content.model.MissionDefinition

interface ContentRepository {
    suspend fun manifest(): ContentManifest

    suspend fun mission(missionId: String): MissionDefinition

    suspend fun chapter(chapterId: String): ChapterDefinition

    suspend fun dialogue(dialogueId: String): DialogueDefinition

    suspend fun listMissions(): List<MissionDefinition>
}
