package com.nullhorizon.app.content

import com.nullhorizon.app.content.model.ChapterDefinition
import com.nullhorizon.app.content.model.ContentManifest
import com.nullhorizon.app.content.model.DialogueDefinition
import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.content.model.RewardDefinition
import com.nullhorizon.app.content.model.SignalDefinition
import com.nullhorizon.app.content.model.SkillDefinition

interface ContentRepository {
    suspend fun manifest(): ContentManifest

    suspend fun mission(missionId: String): MissionDefinition

    suspend fun chapter(chapterId: String): ChapterDefinition

    suspend fun dialogue(dialogueId: String): DialogueDefinition

    suspend fun skill(skillId: String): SkillDefinition

    suspend fun reward(rewardId: String): RewardDefinition

    suspend fun signal(signalId: String): SignalDefinition

    suspend fun listMissions(): List<MissionDefinition>

    suspend fun listSkills(): List<SkillDefinition>

    suspend fun listRewards(): List<RewardDefinition>

    suspend fun listSignals(): List<SignalDefinition>
}
