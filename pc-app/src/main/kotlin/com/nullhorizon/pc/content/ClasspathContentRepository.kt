package com.nullhorizon.pc.content

import com.nullhorizon.app.content.ContentRepository
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

class ClasspathContentRepository(
    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
        ?: ClasspathContentRepository::class.java.classLoader,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = false
    },
) : ContentRepository {
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
            val resourcePath = "$root/$relativePath"
            val text = classLoader.getResourceAsStream(resourcePath)?.bufferedReader()?.use { it.readText() }
                ?: error("Missing classpath resource: $resourcePath")
            json.decodeFromString<T>(text)
        }
}
