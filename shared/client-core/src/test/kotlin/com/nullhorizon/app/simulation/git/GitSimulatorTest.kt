package com.nullhorizon.app.simulation.git

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GitSimulatorTest {
    private val simulator = GitSimulator()

    @Test
    fun statusDiffAddCommitLog_areDeterministic() {
        var state = seededRepo(dirty = true)
        state = simulator.execute(state, "git status")
        assertThat(state.lastStdout).contains("routing.conf")
        assertThat(state.lastStdout).contains("modified")

        state = simulator.execute(state, "git diff")
        assertThat(state.lastStdout).contains("thruster_a=override")

        state = simulator.execute(state, "git add routing.conf")
        state = simulator.execute(state, "git commit -m \"Accept override\"")
        assertThat(state.lastExitCode).isEqualTo(0)
        assertThat(state.headCommit.message).isEqualTo("Accept override")

        state = simulator.execute(state, "git log")
        assertThat(state.lastStdout).contains("Accept override")
        assertThat(state.lastStdout).contains("Sign thruster routing")
    }

    @Test
    fun checkoutDashDash_restoresTrackedFile() {
        var state = seededRepo(dirty = true)
        state = simulator.execute(state, "git checkout -- routing.conf")
        assertThat(state.workingTree["routing.conf"]).contains("thruster_a=nominal")
        assertThat(state.workingTree).isEqualTo(state.headTree())
        assertThat(state.index).isEqualTo(state.headTree())
    }

    @Test
    fun checkoutCommit_restoresHistoricalTree() {
        var state = seededRepo(dirty = false)
        // Advance HEAD with a bad commit, then recover the prior tip.
        state = state.copy(
            workingTree = state.workingTree + ("routing.conf" to "thruster_a=override\nthruster_b=standby\n"),
        )
        state = simulator.execute(state, "git add routing.conf")
        state = simulator.execute(state, "git commit -m \"Bad override\"")
        val badHash = state.headHash
        assertThat(state.workingTree["routing.conf"]).contains("override")

        state = simulator.execute(state, "git log")
        assertThat(state.lastStdout).contains("Sign thruster routing")

        // Resolve the safe commit id from the seeded definition tip before the bad commit.
        val safeHash = state.commits.values.first { it.message == "Sign thruster routing" }.hash
        state = simulator.execute(state, "git checkout $safeHash")
        assertThat(state.lastExitCode).isEqualTo(0)
        assertThat(state.headHash).isEqualTo(safeHash)
        assertThat(state.headHash).isNotEqualTo(badHash)
        assertThat(state.workingTree["routing.conf"]).contains("thruster_a=nominal")
        assertThat(state.workingTree).isEqualTo(state.headTree())
    }

    @Test
    fun branchSwitchMerge_withoutConflict() {
        var state = seededRepo(dirty = false)
        state = simulator.execute(state, "git branch feature")
        state = simulator.execute(state, "git switch feature")
        assertThat(state.currentBranch).isEqualTo("feature")
        state = state.copy(
            workingTree = state.workingTree + ("NOTES.md" to "feature note\n"),
        )
        state = simulator.execute(state, "git add NOTES.md")
        state = simulator.execute(state, "git commit -m \"Add note\"")
        state = simulator.execute(state, "git switch main")
        state = simulator.execute(state, "git merge feature")
        assertThat(state.lastExitCode).isEqualTo(0)
        assertThat(state.headCommit.parents).hasSize(2)
        assertThat(state.workingTree["NOTES.md"]).contains("feature note")
    }

    @Test
    fun mergeConflict_resolveTheirsAddCommit() {
        var state = conflictRepo()
        state = simulator.execute(state, "git merge repair")
        assertThat(state.lastExitCode).isEqualTo(1)
        assertThat(state.conflicts).containsKey("power.policy")
        assertThat(state.mergeParentHash).isNotNull()

        state = simulator.execute(state, "git checkout --theirs power.policy")
        assertThat(state.workingTree["power.policy"]).contains("bus_b=online")
        assertThat(state.conflicts).containsKey("power.policy")

        state = simulator.execute(state, "git add power.policy")
        assertThat(state.conflicts).isEmpty()

        state = simulator.execute(state, "git commit -m \"Merge branch 'repair'\"")
        assertThat(state.lastExitCode).isEqualTo(0)
        assertThat(state.mergeParentHash).isNull()
        assertThat(state.headCommit.parents).hasSize(2)
        assertThat(state.workingTree["power.policy"]!!.trim()).isEqualTo("bus_a=online\nbus_b=online")
    }

    @Test
    fun unsupportedCommand_listsSupported() {
        var state = seededRepo(dirty = false)
        state = simulator.execute(state, "git rebase")
        assertThat(state.lastExitCode).isEqualTo(1)
        assertThat(state.lastStderr).contains("not supported")
        assertThat(state.lastStderr).contains("status")
    }

    @Test
    fun hashes_areDeterministicAcrossRuns() {
        val a = seededRepo(dirty = false)
        val b = seededRepo(dirty = false)
        assertThat(a.headHash).isEqualTo(b.headHash)
        assertThat(a.commits.keys).isEqualTo(b.commits.keys)
    }

    private fun seededRepo(dirty: Boolean): GitRepositoryState {
        val definition = GitRepositoryDefinition(
            initialBranch = "main",
            branches = mapOf("main" to "c1"),
            commits = listOf(
                GitCommitDefinition(
                    id = "c0",
                    message = "Initial vault seed",
                    author = "crew",
                    files = mapOf(
                        "routing.conf" to "thruster_a=nominal\nthruster_b=nominal\n",
                        "README.md" to "Version Vault signed configs.\n",
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
            workingTree = if (dirty) {
                mapOf("routing.conf" to "thruster_a=override\nthruster_b=standby\n")
            } else {
                null
            },
        )
        return GitSimulator.fromDefinition(definition)
    }

    private fun conflictRepo(): GitRepositoryState {
        val definition = GitRepositoryDefinition(
            initialBranch = "main",
            branches = mapOf("main" to "main1", "repair" to "repair1"),
            commits = listOf(
                GitCommitDefinition(
                    id = "base",
                    message = "Baseline power policy",
                    author = "crew",
                    files = mapOf(
                        "power.policy" to "bus_a=unknown\nbus_b=unknown\n",
                        "NOTES.md" to "Shared baseline.\n",
                    ),
                ),
                GitCommitDefinition(
                    id = "main1",
                    message = "Main marks bus_b degraded",
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
        )
        return GitSimulator.fromDefinition(definition)
    }
}
