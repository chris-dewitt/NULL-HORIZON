package com.nullhorizon.pc.data

import com.nullhorizon.app.data.settings.AccessibilitySettings
import com.nullhorizon.app.data.settings.PrivacySettings
import com.nullhorizon.app.data.settings.SettingsRepository
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FileSettingsRepository(
    dataDir: Path,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) : SettingsRepository {
    private val file = dataDir.resolve("settings.json")
    private val mutex = Mutex()
    private val initial = readSnapshot()
    private val _accessibilitySettings = MutableStateFlow(initial.accessibility)
    private val _privacySettings = MutableStateFlow(initial.privacy)

    override val accessibilitySettings: StateFlow<AccessibilitySettings> =
        _accessibilitySettings.asStateFlow()

    override val privacySettings: StateFlow<PrivacySettings> =
        _privacySettings.asStateFlow()

    override suspend fun setHighContrast(enabled: Boolean) {
        update { it.copy(accessibility = it.accessibility.copy(highContrast = enabled)) }
    }

    override suspend fun setReducedMotion(enabled: Boolean) {
        update { it.copy(accessibility = it.accessibility.copy(reducedMotion = enabled)) }
    }

    override suspend fun setLargerText(enabled: Boolean) {
        update { it.copy(accessibility = it.accessibility.copy(largerText = enabled)) }
    }

    override suspend fun setDisableCrt(enabled: Boolean) {
        update { it.copy(accessibility = it.accessibility.copy(disableCrt = enabled)) }
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        update { it.copy(accessibility = it.accessibility.copy(soundEnabled = enabled)) }
    }

    override suspend fun setPaletteId(paletteId: String) {
        update { it.copy(accessibility = it.accessibility.copy(paletteId = paletteId)) }
    }

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        update { it.copy(privacy = it.privacy.copy(analyticsEnabled = enabled)) }
    }

    override suspend fun setCrashReportingEnabled(enabled: Boolean) {
        update { it.copy(privacy = it.privacy.copy(crashReportingEnabled = enabled)) }
    }

    override suspend fun clearAll() {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                Files.deleteIfExists(file)
            }
            publish(SettingsSnapshot())
        }
    }

    private suspend fun update(transform: (SettingsSnapshot) -> SettingsSnapshot) {
        mutex.withLock {
            val next = transform(
                SettingsSnapshot(
                    accessibility = _accessibilitySettings.value,
                    privacy = _privacySettings.value,
                ),
            )
            writeSnapshot(next)
            publish(next)
        }
    }

    private fun publish(snapshot: SettingsSnapshot) {
        _accessibilitySettings.value = snapshot.accessibility
        _privacySettings.value = snapshot.privacy
    }

    private fun readSnapshot(): SettingsSnapshot {
        if (!Files.exists(file)) return SettingsSnapshot()
        return runCatching {
            val text = Files.readString(file)
            if (text.isBlank()) SettingsSnapshot() else json.decodeFromString<SettingsSnapshot>(text)
        }.getOrDefault(SettingsSnapshot())
    }

    private suspend fun writeSnapshot(snapshot: SettingsSnapshot) {
        withContext(Dispatchers.IO) {
            Files.createDirectories(file.parent)
            val temp = file.resolveSibling("${file.fileName}.tmp")
            Files.writeString(temp, json.encodeToString(snapshot))
            runCatching {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            }.getOrElse {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    @Serializable
    private data class SettingsSnapshot(
        val accessibility: AccessibilitySettings = AccessibilitySettings(),
        val privacy: PrivacySettings = PrivacySettings(),
    )
}
