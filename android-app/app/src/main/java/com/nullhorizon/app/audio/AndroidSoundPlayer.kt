package com.nullhorizon.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.nullhorizon.app.R

/**
 * SoundPool-backed player for short UI cues. Every audio call is wrapped so a
 * missing codec, unavailable audio device, or load race degrades to silence
 * rather than crashing the app. Playback is gated by [enabled] (the master
 * sound setting). Call [release] when the owning composition leaves.
 */
class AndroidSoundPlayer(
    context: Context,
    @Volatile var enabled: Boolean = true,
) : SoundPlayer {
    private val appContext = context.applicationContext
    private val pool: SoundPool? = runCatching {
        SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .build()
    }.getOrNull()

    private val soundIds: Map<GameSound, Int> = pool?.let { p ->
        GameSound.entries.mapNotNull { sound ->
            val resId = resourceFor(sound)
            if (resId == 0) return@mapNotNull null
            runCatching { p.load(appContext, resId, 1) }.getOrNull()?.let { sound to it }
        }.toMap()
    } ?: emptyMap()

    override fun play(sound: GameSound) {
        if (!enabled) return
        val p = pool ?: return
        val id = soundIds[sound] ?: return
        runCatching { p.play(id, 1f, 1f, 1, 0, 1f) }
    }

    fun release() {
        runCatching { pool?.release() }
    }

    private fun resourceFor(sound: GameSound): Int = when (sound) {
        GameSound.Success -> R.raw.ui_success
        GameSound.RankUp -> R.raw.ui_rankup
        GameSound.Error -> R.raw.ui_error
    }
}
