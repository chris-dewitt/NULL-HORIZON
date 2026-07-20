package com.nullhorizon.pc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.nullhorizon.app.audio.LocalSoundPlayer
import com.nullhorizon.app.data.settings.AccessibilitySettings
import com.nullhorizon.app.ui.chrome.CrtFrame
import com.nullhorizon.app.ui.chrome.CrtProfile
import com.nullhorizon.app.ui.theme.NullHorizonTheme
import com.nullhorizon.pc.audio.DesktopSoundPlayer
import com.nullhorizon.pc.di.PcAppContainer
import com.nullhorizon.pc.ui.rememberTerminalFontFamily

fun main() = application {
    val container = PcAppContainer()
    Window(
        onCloseRequest = ::exitApplication,
        title = "NULL HORIZON",
    ) {
        val accessibility by container.settingsRepository.accessibilitySettings.collectAsState(
            initial = AccessibilitySettings(),
        )
        val density = LocalDensity.current
        val contentDensity = if (accessibility.largerText) {
            Density(
                density = density.density,
                fontScale = density.fontScale * 1.15f,
            )
        } else {
            density
        }
        val terminalFont = rememberTerminalFontFamily()
        val soundPlayer = remember { DesktopSoundPlayer(enabled = accessibility.soundEnabled) }
        soundPlayer.enabled = accessibility.soundEnabled
        LaunchedEffect(accessibility.soundEnabled) {
            soundPlayer.setAmbient(accessibility.soundEnabled)
        }
        DisposableEffect(Unit) {
            onDispose { soundPlayer.release() }
        }
        NullHorizonTheme(
            highContrast = accessibility.highContrast,
            reducedMotion = accessibility.reducedMotion,
            largerText = accessibility.largerText,
            disableCrt = accessibility.disableCrt,
            fontFamily = terminalFont,
        ) {
            CompositionLocalProvider(
                LocalDensity provides contentDensity,
                LocalSoundPlayer provides soundPlayer,
            ) {
                CrtFrame(
                    modifier = Modifier.fillMaxSize(),
                    profile = CrtProfile.Medium,
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        PcApp(container)
                    }
                }
            }
        }
    }
}
