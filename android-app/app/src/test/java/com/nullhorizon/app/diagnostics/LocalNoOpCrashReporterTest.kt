package com.nullhorizon.app.diagnostics

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class LocalNoOpCrashReporterTest {
    @Test
    fun disabled_ignoresEvents() {
        val reporter = LocalNoOpCrashReporter()
        reporter.recordNonFatal("boom", mapOf("screen" to "settings"))
        assertThat(reporter.isEnabled).isFalse()
    }

    @Test
    fun enabled_rejectsBlockedMetadata() {
        val reporter = LocalNoOpCrashReporter()
        reporter.setEnabled(true)
        assertThrows(IllegalArgumentException::class.java) {
            reporter.recordNonFatal("boom", mapOf("sql" to "select 1"))
        }
    }
}
