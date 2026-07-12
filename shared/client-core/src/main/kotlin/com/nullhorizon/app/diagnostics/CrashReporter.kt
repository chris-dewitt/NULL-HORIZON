package com.nullhorizon.app.diagnostics

/**
 * Opt-in crash reporting boundary. Default implementation never leaves the device
 * and never accepts source, terminal history, SQL, or secrets.
 */
interface CrashReporter {
    val isEnabled: Boolean

    fun setEnabled(enabled: Boolean)

    fun recordNonFatal(message: String, metadata: Map<String, String> = emptyMap())
}

class LocalNoOpCrashReporter : CrashReporter {
    @Volatile
    private var enabled: Boolean = false

    override val isEnabled: Boolean
        get() = enabled

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun recordNonFatal(message: String, metadata: Map<String, String>) {
        if (!enabled) return
        val blocked = listOf("sql", "terminal", "secret", "token", "password", "source")
        require(metadata.keys.none { key -> blocked.any { key.contains(it, ignoreCase = true) } }) {
            "Crash metadata must not include source, SQL, terminal history, or secrets"
        }
        // Intentionally local-only: no network transport in Epic 13.
    }
}
