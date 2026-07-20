package com.nullhorizon.pc.audio

import com.nullhorizon.app.audio.GameSound
import com.nullhorizon.app.audio.SoundPlayer
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

/**
 * javax.sound-backed player for short UI cues on desktop. Mirrors the Android
 * player's contract: every load/play is wrapped so a missing audio device or
 * unreadable clip degrades to silence rather than crashing. Playback is gated
 * by [enabled] (the master sound setting). Call [release] on shutdown.
 */
class DesktopSoundPlayer(
    @Volatile var enabled: Boolean = true,
) : SoundPlayer {
    private val clips: Map<GameSound, Clip> = GameSound.entries.mapNotNull { sound ->
        loadClip("/audio/${sound.assetName}.wav")?.let { sound to it }
    }.toMap()

    private val ambient: Clip? = loadClip("/audio/ambient_hum.wav")

    override fun play(sound: GameSound) {
        if (!enabled) return
        val clip = clips[sound] ?: return
        runCatching {
            clip.stop()
            clip.framePosition = 0
            clip.start()
        }
    }

    override fun setAmbient(enabled: Boolean) {
        val clip = ambient ?: return
        runCatching {
            if (enabled && this.enabled) {
                if (!clip.isRunning) {
                    clip.framePosition = 0
                    clip.loop(Clip.LOOP_CONTINUOUSLY)
                }
            } else {
                clip.stop()
            }
        }
    }

    fun release() {
        clips.values.forEach { runCatching { it.close() } }
        runCatching { ambient?.close() }
    }

    private fun loadClip(resource: String): Clip? = runCatching {
        val url = DesktopSoundPlayer::class.java.getResource(resource) ?: return null
        AudioSystem.getAudioInputStream(url).use { stream ->
            AudioSystem.getClip().apply { open(stream) }
        }
    }.getOrNull()
}
