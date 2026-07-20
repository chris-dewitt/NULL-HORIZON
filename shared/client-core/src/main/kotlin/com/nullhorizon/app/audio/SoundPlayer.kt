package com.nullhorizon.app.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Plays short UI sound cues. Implementations are platform-specific (Android
 * SoundPool, desktop javax.sound) and must fail silently — a missing device or
 * unreadable asset degrades to no sound, never a crash. The default is silent
 * so any surface works without a player wired in.
 */
interface SoundPlayer {
    fun play(sound: GameSound)

    /**
     * Starts or stops the looping ambient reactor hum. Default no-op so a
     * player can adopt ambient audio without breaking the contract.
     */
    fun setAmbient(enabled: Boolean) {}
}

object NoOpSoundPlayer : SoundPlayer {
    override fun play(sound: GameSound) = Unit
}

val LocalSoundPlayer = staticCompositionLocalOf<SoundPlayer> { NoOpSoundPlayer }

/**
 * Plays [sound] once when [key] first appears or changes — the audio companion
 * to a state transition (mission complete, rank up, error). Null key is silent.
 */
@Composable
fun PlaySoundOnce(key: Any?, sound: GameSound) {
    val player = LocalSoundPlayer.current
    LaunchedEffect(key) {
        if (key != null) player.play(sound)
    }
}
