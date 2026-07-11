package com.nullhorizon.app.simulation.terminal

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TerminalSimulatorTest {
    private val fs = VirtualFileSystem.fromEntries(
        listOf(
            VirtualFsEntry("/home/operator", "dir"),
            VirtualFsEntry("/var/log/life_support", "dir"),
            VirtualFsEntry(
                "/var/log/life_support/fault.log",
                "file",
                "INFO valve-1 nominal\nERROR valve-3 pressure unstable\n",
            ),
        ),
    )
    private val simulator = TerminalSimulator(fs)

    @Test
    fun pwd_ls_cd_cat_grep_areDeterministic() {
        var state = simulator.initialState("/home/operator")
        state = simulator.execute(state, "pwd")
        assertThat(state.lastStdout).isEqualTo("/home/operator")

        state = simulator.execute(state, "ls")
        assertThat(state.lastExitCode).isEqualTo(0)
        assertThat(state.lastStdout).isEmpty()

        state = simulator.execute(state, "cd /var/log/life_support")
        assertThat(state.cwd).isEqualTo("/var/log/life_support")
        state = simulator.execute(state, "cat fault.log")
        assertThat(state.lastStdout).contains("valve-3 pressure unstable")
        state = simulator.execute(state, "grep valve-3 fault.log")
        assertThat(state.lastStdout).isEqualTo("ERROR valve-3 pressure unstable")
    }

    @Test
    fun unsupportedSyntax_returnsClearError() {
        var state = simulator.initialState("/home/operator")
        state = simulator.execute(state, "ls | cat")
        assertThat(state.lastExitCode).isEqualTo(2)
        assertThat(state.lastStderr).contains("Pipes and redirection")
    }

    @Test
    fun unknownCommand_listsSupportedCommands() {
        var state = simulator.initialState("/home/operator")
        state = simulator.execute(state, "rm -rf /")
        assertThat(state.lastStderr).contains("Command not found: rm")
        assertThat(state.lastStderr).contains("pwd")
    }

    @Test
    fun vfs_neverRequiresAndroidPaths() {
        val paths = listOf("/home/operator", "/var/log/life_support/fault.log")
        paths.forEach { path ->
            assertThat(path.startsWith("/")).isTrue()
            assertThat(path).doesNotContain("android")
            assertThat(fs.exists(path)).isTrue()
        }
    }
}
