package com.nullhorizon.pc

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.nullhorizon.app.data.settings.AccessibilitySettings
import com.nullhorizon.app.ui.theme.NullHorizonTheme
import com.nullhorizon.pc.di.PcAppContainer

fun main() = application {
    val container = PcAppContainer()
    Window(
        onCloseRequest = ::exitApplication,
        title = "NULL HORIZON",
    ) {
        val accessibility by container.settingsRepository.accessibilitySettings.collectAsState(
            initial = AccessibilitySettings(),
        )
        NullHorizonTheme(
            highContrast = accessibility.highContrast,
            reducedMotion = accessibility.reducedMotion,
        ) {
            PcApp(container)
        }
    }
}
