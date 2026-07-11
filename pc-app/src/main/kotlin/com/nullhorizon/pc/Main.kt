package com.nullhorizon.pc

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.nullhorizon.app.ui.theme.NullHorizonTheme
import com.nullhorizon.pc.di.PcAppContainer

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "NULL HORIZON",
    ) {
        NullHorizonTheme {
            PcApp(PcAppContainer())
        }
    }
}
