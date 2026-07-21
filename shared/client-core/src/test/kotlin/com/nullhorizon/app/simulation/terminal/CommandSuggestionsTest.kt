package com.nullhorizon.app.simulation.terminal

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CommandSuggestionsTest {
    @Test
    fun blankInput_offersFullPalette() {
        assertThat(commandSuggestions("")).isEqualTo(TERMINAL_COMMANDS)
    }

    @Test
    fun prefix_filtersByCommandWord() {
        assertThat(commandSuggestions("c")).containsExactly("cd", "cat").inOrder()
        assertThat(commandSuggestions("gr")).containsExactly("grep")
    }

    @Test
    fun exactMatch_offersNothingMore() {
        assertThat(commandSuggestions("grep")).isEmpty()
    }

    @Test
    fun onceArgumentsStart_noCommandSuggestions() {
        assertThat(commandSuggestions("grep ")).isEmpty()
        assertThat(commandSuggestions("grep valve")).isEmpty()
    }

    @Test
    fun worksForOtherCandidateSets() {
        assertThat(commandSuggestions("co", GIT_SUBCOMMANDS)).containsExactly("commit")
    }
}
