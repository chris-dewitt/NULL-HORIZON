package com.nullhorizon.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nullhorizon.app.navigation.NullHorizonNavHost
import com.nullhorizon.app.ui.theme.NullHorizonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as NullHorizonApplication
        setContent {
            NullHorizonApp(app = app)
        }
    }
}

@Composable
fun NullHorizonApp(
    app: NullHorizonApplication,
) {
    val accessibility by app.container.settingsRepository.accessibilitySettings
        .collectAsStateWithLifecycle(
            initialValue = com.nullhorizon.app.data.settings.AccessibilitySettings(),
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

    NullHorizonTheme(
        highContrast = accessibility.highContrast,
        reducedMotion = accessibility.reducedMotion,
    ) {
        CompositionLocalProvider(LocalDensity provides contentDensity) {
            Surface(modifier = Modifier.fillMaxSize()) {
                NullHorizonNavHost(appContainer = app.container)
            }
        }
    }
}
