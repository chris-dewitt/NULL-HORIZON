package com.nullhorizon.app.simulation.terminal

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CommandCanonicalizationTest {
    @Test
    fun stripsSingleQuotes() {
        assertThat(canonicalizeCommandLine("grep 'ERROR' diag.txt"))
            .isEqualTo("grep ERROR diag.txt")
    }

    @Test
    fun stripsDoubleQuotes() {
        assertThat(canonicalizeCommandLine("grep \"ERROR\" diag.txt"))
            .isEqualTo("grep ERROR diag.txt")
    }

    @Test
    fun collapsesExtraWhitespace() {
        assertThat(canonicalizeCommandLine("  grep   ERROR    diag.txt "))
            .isEqualTo("grep ERROR diag.txt")
    }

    @Test
    fun quotedAndUnquotedAreEqual() {
        assertThat(canonicalizeCommandLine("grep 'valve-3' fault.log"))
            .isEqualTo(canonicalizeCommandLine("grep valve-3 fault.log"))
    }

    @Test
    fun preservesSpacesInsideQuotedGitMessage() {
        assertThat(canonicalizeCommandLine("git commit -m 'repair the valve'"))
            .isEqualTo("git commit -m repair the valve")
    }

    @Test
    fun unterminatedQuoteDoesNotThrow() {
        assertThat(canonicalizeCommandLine("grep 'oops")).isEqualTo("grep oops")
    }
}
