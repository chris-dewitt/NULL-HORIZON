package com.nullhorizon.pc.data

import com.nullhorizon.app.data.profile.LocalProfile
import com.nullhorizon.app.data.profile.LocalProfileRepository
import com.nullhorizon.app.data.profile.LocalProfileValidator
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FileLocalProfileRepository(
    dataDir: Path,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) : LocalProfileRepository {
    private val file = dataDir.resolve("local_profile.json")
    private val mutex = Mutex()
    private val _profile = MutableStateFlow(readProfile())

    override val profile: StateFlow<LocalProfile?> = _profile.asStateFlow()

    override suspend fun save(displayName: String, createdAtEpochMs: Long): LocalProfile {
        val normalized = LocalProfileValidator.normalize(displayName)
        require(LocalProfileValidator.isValid(normalized)) {
            "Display name must be 2-24 characters and use letters, numbers, spaces, _ . or -"
        }
        val next = LocalProfile(displayName = normalized, createdAtEpochMs = createdAtEpochMs)
        mutex.withLock {
            writeProfile(next)
            _profile.value = next
        }
        return next
    }

    override suspend fun clear() {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                Files.deleteIfExists(file)
            }
            _profile.value = null
        }
    }

    private fun readProfile(): LocalProfile? {
        if (!Files.exists(file)) return null
        return runCatching {
            val text = Files.readString(file)
            if (text.isBlank()) null else json.decodeFromString<LocalProfile>(text)
        }.getOrNull()
    }

    private suspend fun writeProfile(profile: LocalProfile) {
        withContext(Dispatchers.IO) {
            Files.createDirectories(file.parent)
            val temp = file.resolveSibling("${file.fileName}.tmp")
            Files.writeString(temp, json.encodeToString(profile))
            runCatching {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            }.getOrElse {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}
