package com.nullhorizon.app.content.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ContentManifest(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("bundle_id") val bundleId: String,
    val version: String,
    @SerialName("min_app_version") val minAppVersion: String,
    @SerialName("content_schema_version") val contentSchemaVersion: Int,
    val locale: String,
    val channel: String,
    val chapters: List<String> = emptyList(),
    val missions: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val dialogues: List<String> = emptyList(),
    val rewards: List<String> = emptyList(),
)

@Serializable
data class MissionDefinition(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("mission_id") val missionId: String,
    val version: String,
    @SerialName("chapter_id") val chapterId: String,
    val title: String,
    val summary: String,
    val difficulty: String,
    val requirements: MissionRequirements,
    val skills: MissionSkills,
    val narrative: MissionNarrative,
    val tools: List<String> = emptyList(),
    val environment: EnvironmentDefinition,
    val objectives: List<ObjectiveDefinition>,
    val hints: List<HintDefinition> = emptyList(),
    val rewards: MissionRewards,
    val completion: CompletionDefinition,
)

@Serializable
data class MissionRequirements(
    @SerialName("app_version") val appVersion: String,
    val online: Boolean,
    @SerialName("prerequisite_skills") val prerequisiteSkills: List<String> = emptyList(),
)

@Serializable
data class MissionSkills(
    val primary: String,
    val secondary: List<String> = emptyList(),
)

@Serializable
data class MissionNarrative(
    @SerialName("briefing_dialogue_id") val briefingDialogueId: String,
    @SerialName("success_dialogue_id") val successDialogueId: String,
    @SerialName("failure_consequence_id") val failureConsequenceId: String? = null,
)

@Serializable
data class EnvironmentDefinition(
    @SerialName("schema_version") val schemaVersion: Int = 1,
    @SerialName("template_id") val templateId: String,
    val seed: Int,
    @SerialName("initial_state") val initialState: Map<String, JsonElement> = emptyMap(),
    val actions: List<MissionActionDefinition> = emptyList(),
    val filesystem: VirtualFilesystemDefinition? = null,
    val git: com.nullhorizon.app.simulation.git.GitRepositoryDefinition? = null,
    val databases: List<com.nullhorizon.app.simulation.sql.MissionDatabaseDefinition> = emptyList(),
    val workspace: com.nullhorizon.app.simulation.execution.WorkspaceDefinition? = null,
    @SerialName("service_map")
    val serviceMap: com.nullhorizon.app.simulation.servicemap.ServiceMapDefinition? = null,
    val pipeline: com.nullhorizon.app.simulation.pipeline.PipelineDefinition? = null,
    val mlops: com.nullhorizon.app.simulation.mlops.MlOpsDefinition? = null,
)

@Serializable
data class VirtualFilesystemDefinition(
    val cwd: String,
    val entries: List<VirtualFilesystemEntry>,
)

@Serializable
data class VirtualFilesystemEntry(
    val path: String,
    val type: String,
    val content: String? = null,
)

@Serializable
data class MissionActionDefinition(
    val id: String,
    val label: String,
    val description: String? = null,
    val requires: Map<String, JsonElement> = emptyMap(),
    val effects: Map<String, JsonElement> = emptyMap(),
)

@Serializable
data class ObjectiveDefinition(
    val id: String,
    val type: String,
    val description: String,
    val visible: Boolean,
    val assert: Map<String, JsonElement> = emptyMap(),
)

@Serializable
data class HintDefinition(
    val level: Int,
    val text: String? = null,
    val pseudocode: String? = null,
)

@Serializable
data class MissionRewards(
    @SerialName("clearance_points") val clearancePoints: Int,
    val mastery: Map<String, Int> = emptyMap(),
    val unlocks: List<String> = emptyList(),
)

@Serializable
data class CompletionDefinition(
    val mode: String,
    @SerialName("objective_ids") val objectiveIds: List<String>,
)

@Serializable
data class DialogueDefinition(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("dialogue_id") val dialogueId: String,
    val lines: List<DialogueLine>,
)

@Serializable
data class DialogueLine(
    val speaker: String,
    val text: String,
)

@Serializable
data class ChapterDefinition(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("chapter_id") val chapterId: String,
    val title: String,
    val region: String,
    val summary: String? = null,
    @SerialName("mission_ids") val missionIds: List<String>,
)

@Serializable
data class SkillDefinition(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("skill_id") val skillId: String,
    val name: String,
    val domain: String,
    val description: String? = null,
    val prerequisites: List<String> = emptyList(),
)

@Serializable
data class RewardDefinition(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("reward_id") val rewardId: String,
    val kind: String,
    val name: String,
    val description: String? = null,
)
