package com.nullhorizon.app.progression

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AuditorLogTest {
    private val fragments = listOf("alpha", "bravo", "charlie", "delta")

    @Test
    fun noFragmentBeforeAnyCompletion() {
        assertThat(AuditorLog.fragmentFor(fragments, 0)).isNull()
        assertThat(AuditorLog.fragmentFor(fragments, -3)).isNull()
    }

    @Test
    fun noFragmentWhenContentEmpty() {
        assertThat(AuditorLog.fragmentFor(emptyList(), 1)).isNull()
    }

    @Test
    fun firstCompletionRevealsFragmentOne() {
        val fragment = AuditorLog.fragmentFor(fragments, 1)
        assertThat(fragment).isNotNull()
        assertThat(fragment!!.index).isEqualTo(1)
        assertThat(fragment.total).isEqualTo(fragments.size)
        assertThat(fragment.text).isEqualTo("alpha")
    }

    @Test
    fun fragmentsAdvanceWithCompletions() {
        assertThat(AuditorLog.fragmentFor(fragments, 3)!!.index).isEqualTo(3)
        assertThat(AuditorLog.fragmentFor(fragments, 3)!!.text)
            .isNotEqualTo(AuditorLog.fragmentFor(fragments, 1)!!.text)
    }

    @Test
    fun capsAtFinalFragmentOnceExhausted() {
        val last = AuditorLog.fragmentFor(fragments, fragments.size)!!
        val beyond = AuditorLog.fragmentFor(fragments, fragments.size + 25)!!
        assertThat(beyond.index).isEqualTo(fragments.size)
        assertThat(beyond.text).isEqualTo(last.text)
    }
}
