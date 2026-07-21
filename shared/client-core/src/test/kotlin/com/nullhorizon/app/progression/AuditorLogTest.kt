package com.nullhorizon.app.progression

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AuditorLogTest {
    @Test
    fun noFragmentBeforeAnyCompletion() {
        assertThat(AuditorLog.fragmentFor(0)).isNull()
        assertThat(AuditorLog.fragmentFor(-3)).isNull()
    }

    @Test
    fun firstCompletionRevealsFragmentOne() {
        val fragment = AuditorLog.fragmentFor(1)
        assertThat(fragment).isNotNull()
        assertThat(fragment!!.index).isEqualTo(1)
        assertThat(fragment.total).isEqualTo(AuditorLog.total)
        assertThat(fragment.text).isNotEmpty()
    }

    @Test
    fun fragmentsAdvanceWithCompletions() {
        assertThat(AuditorLog.fragmentFor(3)!!.index).isEqualTo(3)
        assertThat(AuditorLog.fragmentFor(3)!!.text)
            .isNotEqualTo(AuditorLog.fragmentFor(1)!!.text)
    }

    @Test
    fun capsAtFinalFragmentOnceExhausted() {
        val last = AuditorLog.fragmentFor(AuditorLog.total)!!
        val beyond = AuditorLog.fragmentFor(AuditorLog.total + 25)!!
        assertThat(beyond.index).isEqualTo(AuditorLog.total)
        assertThat(beyond.text).isEqualTo(last.text)
    }
}
