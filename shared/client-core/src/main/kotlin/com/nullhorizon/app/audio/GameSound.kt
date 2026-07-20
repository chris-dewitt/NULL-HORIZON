package com.nullhorizon.app.audio

/**
 * Logical sound cues the game can request. [assetName] is the base filename
 * (no extension) each client maps to its own packaged audio: Android res/raw,
 * PC classpath resources. Keep names lowercase/underscore for Android res ids.
 */
enum class GameSound(val assetName: String) {
    Success("ui_success"),
    RankUp("ui_rankup"),
    Error("ui_error"),
    Click("ui_click"),
}
