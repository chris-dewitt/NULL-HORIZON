package com.nullhorizon.app.content

import android.content.Context
import com.nullhorizon.app.content.model.ChapterDefinition
import com.nullhorizon.app.content.model.ContentManifest
import com.nullhorizon.app.content.model.DialogueDefinition
import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.content.model.RewardDefinition
import com.nullhorizon.app.content.model.SignalDefinition
import com.nullhorizon.app.content.model.SkillDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class AssetContentRepository(
    context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = false
    },
) : ContentRepository {
    private val assets = context.applicationContext.assets
    private val root = "content"

    override suspend fun manifest(): ContentManifest = decode("manifest.json")

    override suspend fun mission(missionId: String): MissionDefinition =
        decode("missions/$missionId.json")

    override suspend fun chapter(chapterId: String): ChapterDefinition =
        decode("chapters/$chapterId.json")

    override suspend fun dialogue(dialogueId: String): DialogueDefinition =
        decode("dialogues/$dialogueId.json")

    override suspend fun skill(skillId: String): SkillDefinition =
        decode("skills/$skillId.json")

    override suspend fun reward(rewardId: String): RewardDefinition =
        decode("rewards/$rewardId.json")

    override suspend fun signal(signalId: String): SignalDefinition =
        decode("signals/$signalId.json")

    override suspend fun listMissions(): List<MissionDefinition> {
        val manifest = manifest()
        return manifest.missions.map { mission(it) }
    }

    override suspend fun listSkills(): List<SkillDefinition> {
        val manifest = manifest()
        return manifest.skills.map { skill(it) }
    }

    override suspend fun listRewards(): List<RewardDefinition> {
        val manifest = manifest()
        return manifest.rewards.map { reward(it) }
    }

    override suspend fun listSignals(): List<SignalDefinition> {
        val manifest = manifest()
        return manifest.signals.map { signal(it) }
    }

    private suspend inline fun <reified T> decode(relativePath: String): T =
        withContext(Dispatchers.IO) {
            val text = assets.open("$root/$relativePath").bufferedReader().use { it.readText() }
            json.decodeFromString<T>(text)
        }
}
