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

class RecoverCommitMissionTest {
    private val machine = MissionStateMachine(recoverCommitMission())

    @Test
    fun checkoutSafeCommit_completesMission() {
        var state = machine.begin(machine.initialState())
        assertThat(state.git!!.headHash).isEqualTo("c2")
        assertThat(state.git!!.workingTree["routing.conf"]).contains("override")

        state = machine.runGitCommand(state, "git log")
        assertThat(state.completedObjectiveIds).contains("inspect_log")

        state = machine.runGitCommand(state, "git checkout c1")
        assertThat(state.phase).isEqualTo(MissionPhase.Completed)
        assertThat(state.git!!.headHash).isEqualTo("c1")
        assertThat(state.git!!.workingTree["routing.conf"]).contains("thruster_a=nominal")
    }

    private fun recoverCommitMission(): MissionDefinition {
        return MissionDefinition(
            schemaVersion = 1,
            missionId = "version.recover_commit.01",
            version = "1.0.0",
            chapterId = "version_vault",
            title = "Recover a Safe Commit",
            summary = "Recover",
            difficulty = "introductory",
            requirements = MissionRequirements(appVersion = ">=0.1.0", online = false),
            skills = MissionSkills(primary = "git.recover"),
            narrative = MissionNarrative(
                briefingDialogueId = "dialogue.version.recover_commit.briefing",
                successDialogueId = "dialogue.version.recover_commit.success",
            ),
            tools = listOf("git"),
            environment = EnvironmentDefinition(
                templateId = "git.repo.v1",
                seed = 13,
                git = GitRepositoryDefinition(
                    initialBranch = "main",
                    branches = mapOf("main" to "c2"),
                    commits = listOf(
                        GitCommitDefinition(
                            id = "c0",
                            message = "Initial vault seed",
                            author = "crew",
                            files = mapOf("routing.conf" to "thruster_a=nominal\nthruster_b=nominal\n"),
                        ),
                        GitCommitDefinition(
                            id = "c1",
                            message = "Sign thruster routing",
                            author = "orion",
                            parent = "c0",
                            files = mapOf("routing.conf" to "thruster_a=nominal\nthruster_b=standby\n"),
                        ),
                        GitCommitDefinition(
                            id = "c2",
                            message = "Unsigned thruster override",
                            author = "unknown",
                            parent = "c1",
                            files = mapOf("routing.conf" to "thruster_a=override\nthruster_b=standby\n"),
                        ),
                    ),
                ),
            ),
            objectives = listOf(
                ObjectiveDefinition(
                    id = "inspect_log",
                    type = "git_state",
                    description = "log",
                    visible = true,
                    assert = mapOf(
                        "last_command" to JsonPrimitive("git log"),
                        "stdout_contains" to JsonPrimitive("Sign thruster routing"),
                    ),
                ),
                ObjectiveDefinition(
                    id = "recover_signed",
                    type = "git_state",
                    description = "checkout",
                    visible = true,
                    assert = mapOf(
                        "head_hash" to JsonPrimitive("c1"),
                        "head_message" to JsonPrimitive("Sign thruster routing"),
                        "working_tree_clean" to JsonPrimitive("true"),
                        "file_equals:routing.conf" to JsonPrimitive(
                            "thruster_a=nominal\nthruster_b=standby\n",
                        ),
                    ),
                ),
            ),
            hints = listOf(HintDefinition(level = 1, text = "log")),
            rewards = MissionRewards(clearancePoints = 35),
            completion = CompletionDefinition(
                mode = "all",
                objectiveIds = listOf("inspect_log", "recover_signed"),
            ),
        )
    }
}
