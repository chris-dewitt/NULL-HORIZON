package com.nullhorizon.app.feature.mission.engine

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.app.content.model.CompletionDefinition
import com.nullhorizon.app.content.model.EnvironmentDefinition
import com.nullhorizon.app.content.model.HintDefinition
import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.content.model.MissionNarrative
import com.nullhorizon.app.content.model.MissionRequirements
import com.nullhorizon.app.content.model.MissionRewards
import com.nullhorizon.app.content.model.MissionSkills
import com.nullhorizon.app.content.model.ObjectiveDefinition
import com.nullhorizon.app.simulation.git.GitCommitDefinition
import com.nullhorizon.app.simulation.git.GitRepositoryDefinition
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class GitMissionTest {
    @Test
    fun unsignedChange_happyPathAndReset() {
        val machine = MissionStateMachine(unsignedChangeMission())
        var state = machine.begin(machine.initialState())
        assertThat(state.git).isNotNull()

        state = machine.runGitCommand(state, "git status")
        assertThat(state.completedObjectiveIds).contains("inspect_status")

        state = machine.runGitCommand(state, "git diff")
        assertThat(state.completedObjectiveIds).containsAtLeast("inspect_status", "inspect_diff")

        state = machine.runGitCommand(state, "git checkout -- routing.conf")
        assertThat(state.phase).isEqualTo(MissionPhase.Completed)
        assertThat(state.git!!.workingTree["routing.conf"]).contains("thruster_a=nominal")

        val reset = machine.reset()
        assertThat(reset.completedObjectiveIds).isEmpty()
        assertThat(reset.git!!.workingTree["routing.conf"]).contains("thruster_a=override")
        assertThat(reset.git!!.history).isEmpty()
    }

    @Test
    fun mergeConflict_completeAndReset() {
        val machine = MissionStateMachine(mergeConflictMission())
        var state = machine.begin(machine.initialState())

        state = machine.runGitCommand(state, "git merge repair")
        assertThat(state.completedObjectiveIds).contains("start_merge")
        assertThat(state.git!!.conflicts).containsKey("power.policy")

        state = machine.runGitCommand(state, "git checkout --theirs power.policy")
        assertThat(state.completedObjectiveIds).contains("resolve_theirs")

        state = machine.runGitCommand(state, "git add power.policy")
        state = machine.runGitCommand(state, "git commit -m \"Merge branch 'repair'\"")
        assertThat(state.phase).isEqualTo(MissionPhase.Completed)
        assertThat(state.git!!.headCommit.parents).hasSize(2)

        val reset = machine.reset()
        assertThat(reset.phase).isEqualTo(MissionPhase.Briefing)
        assertThat(reset.git!!.conflicts).isEmpty()
        assertThat(reset.git!!.currentBranch).isEqualTo("main")
        assertThat(reset.completedObjectiveIds).isEmpty()
    }

    private fun unsignedChangeMission(): MissionDefinition {
        return MissionDefinition(
            schemaVersion = 1,
            missionId = "version.unsigned_change.01",
            version = "1.0.0",
            chapterId = "version_vault",
            title = "Unsigned Change",
            summary = "Discard unauthorized edit",
            difficulty = "introductory",
            requirements = MissionRequirements(appVersion = ">=0.1.0", online = false),
            skills = MissionSkills(primary = "git.inspect"),
            narrative = MissionNarrative(
                briefingDialogueId = "dialogue.version.unsigned_change.briefing",
                successDialogueId = "dialogue.version.unsigned_change.success",
            ),
            tools = listOf("git"),
            environment = EnvironmentDefinition(
                templateId = "git.repo.v1",
                seed = 11,
                git = GitRepositoryDefinition(
                    initialBranch = "main",
                    branches = mapOf("main" to "c1"),
                    commits = listOf(
                        GitCommitDefinition(
                            id = "c0",
                            message = "Initial vault seed",
                            author = "crew",
                            files = mapOf(
                                "routing.conf" to "thruster_a=nominal\nthruster_b=nominal\n",
                            ),
                        ),
                        GitCommitDefinition(
                            id = "c1",
                            message = "Sign thruster routing",
                            author = "orion",
                            parent = "c0",
                            files = mapOf(
                                "routing.conf" to "thruster_a=nominal\nthruster_b=standby\n",
                            ),
                        ),
                    ),
                    workingTree = mapOf(
                        "routing.conf" to "thruster_a=override\nthruster_b=standby\n",
                    ),
                ),
            ),
            objectives = listOf(
                ObjectiveDefinition(
                    id = "inspect_status",
                    type = "git_state",
                    description = "Status",
                    visible = true,
                    assert = mapOf(
                        "last_command" to JsonPrimitive("git status"),
                        "working_tree_clean" to JsonPrimitive(false),
                        "stdout_contains" to JsonPrimitive("routing.conf"),
                    ),
                ),
                ObjectiveDefinition(
                    id = "inspect_diff",
                    type = "git_state",
                    description = "Diff",
                    visible = true,
                    assert = mapOf(
                        "last_command" to JsonPrimitive("git diff"),
                        "stdout_contains" to JsonPrimitive("thruster_a=override"),
                    ),
                ),
                ObjectiveDefinition(
                    id = "restore_signed",
                    type = "git_state",
                    description = "Restore",
                    visible = true,
                    assert = mapOf(
                        "working_tree_clean" to JsonPrimitive(true),
                        "file_equals:routing.conf" to JsonPrimitive(
                            "thruster_a=nominal\nthruster_b=standby",
                        ),
                    ),
                ),
            ),
            hints = listOf(HintDefinition(level = 1, text = "git status")),
            rewards = MissionRewards(clearancePoints = 35),
            completion = CompletionDefinition(
                mode = "all",
                objectiveIds = listOf("inspect_status", "inspect_diff", "restore_signed"),
            ),
        )
    }

    private fun mergeConflictMission(): MissionDefinition {
        return MissionDefinition(
            schemaVersion = 1,
            missionId = "version.merge_conflict.01",
            version = "1.0.0",
            chapterId = "version_vault",
            title = "Conflicting Repair",
            summary = "Merge with conflict",
            difficulty = "practiced",
            requirements = MissionRequirements(appVersion = ">=0.1.0", online = false),
            skills = MissionSkills(primary = "git.merge"),
            narrative = MissionNarrative(
                briefingDialogueId = "dialogue.version.merge_conflict.briefing",
                successDialogueId = "dialogue.version.merge_conflict.success",
            ),
            tools = listOf("git"),
            environment = EnvironmentDefinition(
                templateId = "git.repo.v1",
                seed = 13,
                git = GitRepositoryDefinition(
                    initialBranch = "main",
                    branches = mapOf("main" to "main1", "repair" to "repair1"),
                    commits = listOf(
                        GitCommitDefinition(
                            id = "base",
                            message = "Baseline power policy",
                            author = "crew",
                            files = mapOf(
                                "power.policy" to "bus_a=online\nbus_b=online\n",
                                "NOTES.md" to "Shared baseline.\n",
                            ),
                        ),
                        GitCommitDefinition(
                            id = "main1",
                            message = "Main keeps bus_a online",
                            author = "orion",
                            parent = "base",
                            files = mapOf(
                                "power.policy" to "bus_a=online\nbus_b=degraded\n",
                            ),
                        ),
                        GitCommitDefinition(
                            id = "repair1",
                            message = "Repair restores both buses",
                            author = "mica",
                            parent = "base",
                            files = mapOf(
                                "power.policy" to "bus_a=online\nbus_b=online\n",
                                "NOTES.md" to "Shared baseline.\nRepair notes attached.\n",
                            ),
                        ),
                    ),
                ),
            ),
            objectives = listOf(
                ObjectiveDefinition(
                    id = "start_merge",
                    type = "git_state",
                    description = "Merge",
                    visible = true,
                    assert = mapOf(
                        "last_command" to JsonPrimitive("git merge repair"),
                        "conflict_count" to JsonPrimitive(1),
                        "stdout_contains" to JsonPrimitive("CONFLICT"),
                    ),
                ),
                ObjectiveDefinition(
                    id = "resolve_theirs",
                    type = "git_state",
                    description = "Resolve",
                    visible = true,
                    assert = mapOf(
                        "file_equals:power.policy" to JsonPrimitive(
                            "bus_a=online\nbus_b=online",
                        ),
                    ),
                ),
                ObjectiveDefinition(
                    id = "finish_merge",
                    type = "git_state",
                    description = "Commit",
                    visible = true,
                    assert = mapOf(
                        "working_tree_clean" to JsonPrimitive(true),
                        "conflict_count" to JsonPrimitive(0),
                        "head_message_contains" to JsonPrimitive("Merge"),
                        "file_equals:power.policy" to JsonPrimitive(
                            "bus_a=online\nbus_b=online",
                        ),
                    ),
                ),
            ),
            hints = listOf(HintDefinition(level = 1, text = "git merge repair")),
            rewards = MissionRewards(clearancePoints = 45),
            completion = CompletionDefinition(
                mode = "all",
                objectiveIds = listOf("start_merge", "resolve_theirs", "finish_merge"),
            ),
        )
    }
}
